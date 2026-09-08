package cn.iocoder.yudao.module.ai.data.governance.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.DataAssetContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据资产正文 Mapper。
 */
@Mapper
public interface DataAssetContentMapper extends BaseMapperX<DataAssetContent> {

    default DataAssetContent selectByAssetId(Long assetId) {
        return selectOne(DataAssetContent::getAssetId, assetId);
    }

    default int updateByAssetId(DataAssetContent content) {
        return update(content, new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<DataAssetContent>()
                .eq(DataAssetContent::getAssetId, content.getAssetId()));
    }

}
