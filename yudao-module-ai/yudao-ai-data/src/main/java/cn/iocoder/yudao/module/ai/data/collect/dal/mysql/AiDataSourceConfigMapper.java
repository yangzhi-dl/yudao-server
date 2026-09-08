package cn.iocoder.yudao.module.ai.data.collect.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置 Mapper。
 */
@Mapper
public interface AiDataSourceConfigMapper extends BaseMapperX<DataSourceConfig> {

    default PageResult<DataSourceConfig> selectPage(PageParam pageParam, Integer sourceType, String name) {
        LambdaQueryWrapperX<DataSourceConfig> wrapper = new LambdaQueryWrapperX<DataSourceConfig>()
                .eqIfPresent(DataSourceConfig::getSourceType, sourceType)
                .likeIfPresent(DataSourceConfig::getName, name)
                .orderByDesc(DataSourceConfig::getCreateTime);
        return selectPage(pageParam, wrapper);
    }

}
