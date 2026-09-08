package cn.iocoder.yudao.module.ai.common.dal.redis;

/**
 * AI Redis Key 枚举类
 *
 * @author yudao
 */
public interface RedisKeyConstants {

    /**
     * 任务序号的缓存
     *
     * KEY 格式：ai:seq_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String TASK_NO = "ai:seq_no:task:";

    /**
     * 文档转换进度缓存
     *
     * KEY 格式：ai:doc_convert:{documentId}
     * VALUE 数据格式：已完成页面数
     */
    String DOC_CONVERT_PROGRESS = "ai:doc_convert:";

}
