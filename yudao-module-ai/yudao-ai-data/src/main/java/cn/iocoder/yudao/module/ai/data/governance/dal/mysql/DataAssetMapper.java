package cn.iocoder.yudao.module.ai.data.governance.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.DataAsset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据资产 Mapper。
 */
@Mapper
public interface DataAssetMapper extends BaseMapperX<DataAsset> {

    default PageResult<DataAsset> selectPage(PageParam pageParam, Integer status, String title) {
        LambdaQueryWrapperX<DataAsset> wrapper = new LambdaQueryWrapperX<DataAsset>()
                .eqIfPresent(DataAsset::getStatus, status)
                .likeIfPresent(DataAsset::getTitle, title)
                .orderByDesc(DataAsset::getCreateTime);
        return selectPage(pageParam, wrapper);
    }

}
