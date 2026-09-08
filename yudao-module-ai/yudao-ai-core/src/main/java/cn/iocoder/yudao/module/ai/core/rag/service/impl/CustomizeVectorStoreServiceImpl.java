package cn.iocoder.yudao.module.ai.core.rag.service.impl;

import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelSettingDO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatSettingService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatModelService;
import cn.iocoder.yudao.module.ai.core.rag.config.MilvusConfig;
import cn.iocoder.yudao.module.ai.core.rag.service.CustomizeVectorStoreService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.DeleteParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CustomizeVectorStoreServiceImpl implements CustomizeVectorStoreService, DisposableBean {

    private final AiChatModelService aiChatModelService;
    private final AiChatSettingService aiChatSettingService;
    private final MilvusConfig milvusConfig;

    // 单例管理 MilvusServiceClient，避免重复创建
    private volatile MilvusServiceClient milvusClient;
    private final Object clientLock = new Object();

    public CustomizeVectorStoreServiceImpl(
            AiChatModelService aiChatModelService,
            AiChatSettingService aiChatSettingService,
            MilvusConfig milvusConfig) {
        this.aiChatModelService = aiChatModelService;
        this.aiChatSettingService = aiChatSettingService;
        this.milvusConfig = milvusConfig;
    }

    /**
     * 懒汉式单例 + 双重检查锁，获取/创建 Milvus 客户端
     */
    private MilvusServiceClient getOrCreateClient() {
        if (milvusClient == null) {
            synchronized (clientLock) {
                if (milvusClient == null) {
                    milvusClient = createMilvusClient();
                    log.info("Milvus client initialized: {}:{}",
                            milvusConfig.getHost(), milvusConfig.getPort());
                }
            }
        }
        return milvusClient;
    }

    /**
     * 封装客户端创建逻辑，包含完整保活配置
     */
    private MilvusServiceClient createMilvusClient() {
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withHost(milvusConfig.getHost())
                .withPort(milvusConfig.getPort())
                .withDatabaseName(milvusConfig.getDatabase())
                .withConnectTimeout(milvusConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .withKeepAliveTime(milvusConfig.getKeepAliveTime(), TimeUnit.SECONDS)
                .withKeepAliveTimeout(milvusConfig.getKeepAliveTimeout(), TimeUnit.SECONDS)
                .withIdleTimeout(milvusConfig.getIdleTimeout(), TimeUnit.DAYS);
        if (StringUtils.isNotBlank(milvusConfig.getToken())) {
            builder.withToken(milvusConfig.getToken());
        }

        return new MilvusServiceClient(builder.build());
    }

    @Override
    public VectorStore loadMilvusVectorStore() {
        String databaseName = milvusConfig.getDatabase();
        String collectionName = milvusConfig.getCollection();
        try {

            ModelSettingDO userModelSetting = aiChatSettingService.getUserModelSetting();
            EmbeddingModel embeddingModel = aiChatModelService
                    .getEmbeddingModel(userModelSetting.getEmbeddingModel());

            // 复用单例客户端
            MilvusServiceClient client = getOrCreateClient();
            return MilvusVectorStore.builder(client, embeddingModel)
                    .databaseName(databaseName)
                    .collectionName(collectionName)
                    .indexType(IndexType.IVF_FLAT)
                    .metricType(MetricType.COSINE)
                    .iDFieldName("id")
                    .batchingStrategy(new TokenCountBatchingStrategy())
                    .build();

        } catch (Exception e) {
            log.error("Failed to load Milvus vector store: databaseName={}, collectionName={}",
                    databaseName, collectionName, e);
            throw new RuntimeException("初始化向量存储失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMilvus(String expr) {
        String databaseName = milvusConfig.getDatabase();
        String collectionName = milvusConfig.getCollection();
        try {
            if (StringUtils.isBlank(expr)) {
                log.warn("Invalid delete params: collectionName={}, expr={}", collectionName, expr);
                return;
            }

            // 复用单例客户端
            MilvusServiceClient client = getOrCreateClient();

            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDatabaseName(databaseName)
                    .withExpr(expr)
                    .build();

            R<MutationResult> result = client.delete(deleteParam);
            log.debug("Delete success: collection={}, expr={}, deletedCount={}",
                    collectionName, expr, result.getData().getDeleteCnt());

        } catch (Exception e) {
            log.error("Delete milvus data error: databaseName={}, collectionName={}, expr={}",
                    databaseName, collectionName, expr, e);
            throw new RuntimeException("删除向量数据异常: " + e.getMessage(), e);
        }
    }

    /**
     * 应用关闭时释放资源
     */
    @Override
    public void destroy() {
        if (milvusClient != null) {
            try {
                milvusClient.close();
                log.info("Milvus client closed successfully");
            } catch (Exception e) {
                log.error("Failed to close Milvus client", e);
            }
        }
    }

}