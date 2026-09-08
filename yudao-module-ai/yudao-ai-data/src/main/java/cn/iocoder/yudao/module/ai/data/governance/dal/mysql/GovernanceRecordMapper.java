package cn.iocoder.yudao.module.ai.data.governance.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.GovernanceRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据治理记录 Mapper。
 */
@Mapper
public interface GovernanceRecordMapper extends BaseMapperX<GovernanceRecord> {

    default List<GovernanceRecord> selectByAssetId(Long assetId) {
        return selectList(new LambdaQueryWrapperX<GovernanceRecord>()
                .eq(GovernanceRecord::getAssetId, assetId)
                .orderByDesc(GovernanceRecord::getCreateTime));
    }

}
