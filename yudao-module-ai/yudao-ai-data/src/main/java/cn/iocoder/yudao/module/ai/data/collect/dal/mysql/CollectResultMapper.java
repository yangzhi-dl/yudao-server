package cn.iocoder.yudao.module.ai.data.collect.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectResult;
import cn.iocoder.yudao.module.ai.data.collect.enums.CollectResultStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采集结果 Mapper。
 */
@Mapper
public interface CollectResultMapper extends BaseMapperX<CollectResult> {

    default PageResult<CollectResult> selectPage(PageParam pageParam, Long taskId, Integer status) {
        LambdaQueryWrapperX<CollectResult> wrapper = new LambdaQueryWrapperX<CollectResult>()
                .eqIfPresent(CollectResult::getTaskId, taskId)
                .eqIfPresent(CollectResult::getStatus, status)
                .orderByDesc(CollectResult::getCreateTime);
        return selectPage(pageParam, wrapper);
    }

    /**
     * 查询相同文件摘要且采集成功的记录（用于去重）。
     */
    default CollectResult selectSuccessByHash(String fileHash) {
        if (fileHash == null) {
            return null;
        }
        return selectFirstOne(CollectResult::getFileHash, fileHash,
                CollectResult::getStatus, CollectResultStatus.SUCCESS.getValue());
    }

}
