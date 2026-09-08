package cn.iocoder.yudao.module.ai.data.collect.service.impl;

import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectResult;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectTask;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.dal.mysql.CollectResultMapper;
import cn.iocoder.yudao.module.ai.data.collect.dal.mysql.CollectTaskMapper;
import cn.iocoder.yudao.module.ai.data.collect.dal.mysql.AiDataSourceConfigMapper;
import cn.iocoder.yudao.module.ai.data.collect.enums.CollectResultStatus;
import cn.iocoder.yudao.module.ai.data.collect.enums.CollectTaskStatus;
import cn.iocoder.yudao.module.ai.data.collect.enums.ConvertStatus;
import cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType;
import cn.iocoder.yudao.module.ai.data.collect.model.SourceFile;
import cn.iocoder.yudao.module.ai.data.collect.service.DataSource;
import cn.iocoder.yudao.module.ai.data.collect.service.DataSourceRegistry;
import cn.iocoder.yudao.module.ai.data.config.AiDataProperties;
import cn.iocoder.yudao.module.ai.data.governance.service.DataAssetService;
import cn.iocoder.yudao.module.ai.data.governance.service.DataMarkdownAssembler;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * 采集流水线。
 * <p>
 * 编排：拉取采集产物 -> 去重 -> 转 Markdown -> 落仓 -> 生成数据资产。
 */
@Slf4j
@Component
public class CollectPipeline {

    private final CollectTaskMapper collectTaskMapper;
    private final CollectResultMapper collectResultMapper;
    private final AiDataSourceConfigMapper dataSourceConfigMapper;
    private final DataSourceRegistry dataSourceRegistry;
    private final DataMarkdownAssembler dataMarkdownAssembler;
    private final FileService fileService;
    private final AiDataProperties properties;
    private final DataAssetService dataAssetService;

    public CollectPipeline(CollectTaskMapper collectTaskMapper,
                           CollectResultMapper collectResultMapper,
                           AiDataSourceConfigMapper dataSourceConfigMapper,
                           DataSourceRegistry dataSourceRegistry,
                           DataMarkdownAssembler dataMarkdownAssembler,
                           FileService fileService,
                           AiDataProperties properties,
                           DataAssetService dataAssetService) {
        this.collectTaskMapper = collectTaskMapper;
        this.collectResultMapper = collectResultMapper;
        this.dataSourceConfigMapper = dataSourceConfigMapper;
        this.dataSourceRegistry = dataSourceRegistry;
        this.dataMarkdownAssembler = dataMarkdownAssembler;
        this.fileService = fileService;
        this.properties = properties;
        this.dataAssetService = dataAssetService;
    }

    /**
     * 执行采集任务。
     */
    public void run(Long taskId) {
        CollectTask task = collectTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        DataSourceConfig sourceConfig = dataSourceConfigMapper.selectById(task.getSourceConfigId());
        if (sourceConfig == null) {
            markTaskFail(task, "采集源不存在");
            return;
        }

        DataSource dataSource = dataSourceRegistry.get(DataSourceType.fromValue(sourceConfig.getSourceType()));
        if (dataSource == null) {
            markTaskFail(task, "采集源适配器不存在");
            return;
        }

        List<SourceFile> files;
        try {
            files = dataSource.fetch(sourceConfig);
        } catch (Exception e) {
            log.error("采集拉取失败，taskId: {}", taskId, e);
            markTaskFail(task, "采集失败: " + e.getMessage());
            return;
        }

        task.setTotalCount(files.size());
        int success = 0;
        int fail = 0;
        for (SourceFile file : files) {
            try {
                // 去重：相同摘要且采集成功过，跳过
                CollectResult existing = collectResultMapper.selectSuccessByHash(file.getFileHash());
                if (existing != null) {
                    insertResult(taskId, file, CollectResultStatus.SKIP, ConvertStatus.NO_NEED, null, null);
                    continue;
                }

                String markdown = dataMarkdownAssembler.assemble(file);
                String warehouseName = safeFileName(file.getFileName()) + ".md";
                FileDO fileDO = fileService.createFileToData(
                        markdown.getBytes(StandardCharsets.UTF_8),
                        warehouseName,
                        properties.getWarehouseDirectory(),
                        "text/markdown");

                CollectResult result = insertResult(taskId, file, CollectResultStatus.SUCCESS,
                        ConvertStatus.CONVERTED, fileDO.getId(), null);
                Long assetId = dataAssetService.createAsset(sourceConfig.getId(), taskId,
                        result.getId(), fileDO.getId(), file.getFileName(), markdown);
                result.setAssetId(assetId);
                collectResultMapper.updateById(result);
                success++;
            } catch (Exception e) {
                log.warn("采集文件处理失败，taskId: {}, path: {}", taskId, file.getPath(), e);
                insertResult(taskId, file, CollectResultStatus.FAIL, ConvertStatus.FAILED, null, e.getMessage());
                fail++;
            } finally {
                deleteTempFile(file);
            }
        }

        task.setSuccessCount(success);
        task.setFailCount(fail);
        task.setStatus(success > 0
                ? (fail > 0 ? CollectTaskStatus.PARTIAL.getValue() : CollectTaskStatus.SUCCESS.getValue())
                : CollectTaskStatus.FAIL.getValue());
        collectTaskMapper.updateById(task);
    }

    private void markTaskFail(CollectTask task, String remark) {
        task.setStatus(CollectTaskStatus.FAIL.getValue());
        task.setRemark(remark);
        collectTaskMapper.updateById(task);
    }

    private CollectResult insertResult(Long taskId, SourceFile file, CollectResultStatus status,
                                       ConvertStatus convertStatus, Long warehouseFileId, String errorMsg) {
        CollectResult result = CollectResult.builder()
                .taskId(taskId)
                .sourcePath(file.getPath())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize() > 0 ? file.getFileSize() : null)
                .fileHash(file.getFileHash())
                .warehouseFileId(warehouseFileId)
                .status(status.getValue())
                .convertStatus(convertStatus.getValue())
                .errorMsg(errorMsg)
                .build();
        collectResultMapper.insert(result);
        return result;
    }

    private void deleteTempFile(SourceFile file) {
        if (file.getLocalFile() == null) {
            return;
        }
        try {
            Files.deleteIfExists(file.getLocalFile());
        } catch (IOException e) {
            log.warn("删除采集临时文件失败: {}", file.getLocalFile(), e);
        }
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "collect";
        }
        return fileName;
    }

}
