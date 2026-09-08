package cn.iocoder.yudao.module.ai.data.collect.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采集任务 Mapper。
 */
@Mapper
public interface CollectTaskMapper extends BaseMapperX<CollectTask> {

    default PageResult<CollectTask> selectPage(PageParam pageParam, Long sourceConfigId) {
        LambdaQueryWrapperX<CollectTask> wrapper = new LambdaQueryWrapperX<CollectTask>()
                .eqIfPresent(CollectTask::getSourceConfigId, sourceConfigId)
                .orderByDesc(CollectTask::getCreateTime);
        return selectPage(pageParam, wrapper);
    }

}
