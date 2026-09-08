package cn.iocoder.yudao.module.ai.common.service;


import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import cn.iocoder.yudao.module.ai.common.model.entity.UsageRecord;

import java.util.List;

public interface UsageRecordService {

    void insert(UsageRecord record);

    void refreshUsageRecord(UsageRecord record);

    void updateTime(UsageRecordType type, Long objectId);

    Boolean existsByTypeAndObjectId(UsageRecordType type, Long objectId);

    List<UsageRecord> selectUserListByType(UsageRecordType type, Integer limit);

    default List<UsageRecord> selectUserListByType(UsageRecordType type) {
        return selectUserListByType(type, null);
    }

}
