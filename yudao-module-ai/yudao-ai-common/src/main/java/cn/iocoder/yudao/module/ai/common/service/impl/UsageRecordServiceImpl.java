package cn.iocoder.yudao.module.ai.common.service.impl;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.common.dal.dataobject.UsageRecordDO;
import cn.iocoder.yudao.module.ai.common.dal.mysql.UsageRecordMapper;
import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import cn.iocoder.yudao.module.ai.common.model.entity.UsageRecord;
import cn.iocoder.yudao.module.ai.common.service.UsageRecordService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 使用记录 Service 实现
 *
 * @author yudao
 */
@Service
@Validated
public class UsageRecordServiceImpl implements UsageRecordService {

    @Resource
    private UsageRecordMapper usageRecordMapper;

    @Override
    public void insert(UsageRecord record) {
        UsageRecordDO entity = BeanUtils.toBean(record, UsageRecordDO.class);
        usageRecordMapper.insert(entity);
    }

    @Override
    public void refreshUsageRecord(UsageRecord record) {
        if (!this.existsByTypeAndObjectId(record.getType(), record.getObjectId())) {
            this.insert(record);
        } else {
            this.updateTime(record.getType(), record.getObjectId());
        }
    }

    @Override
    public void updateTime(UsageRecordType type, Long objectId) {
        String loginUserId = String.valueOf(getLoginUserId());
        usageRecordMapper.updateTime(LocalDateTime.now(), type, objectId, loginUserId);
    }

    @Override
    public Boolean existsByTypeAndObjectId(UsageRecordType type, Long objectId) {
        String loginUserId = String.valueOf(getLoginUserId());
        return usageRecordMapper.existsByTypeAndObjectId(type, objectId, loginUserId);
    }

    @Override
    public List<UsageRecord> selectUserListByType(UsageRecordType type, Integer limit) {
        String loginUserId = String.valueOf(getLoginUserId());
        List<UsageRecordDO> dos = usageRecordMapper.selectUserListByType(type, loginUserId, limit);
        return dos.stream()
                .map(doObj -> BeanUtils.toBean(doObj, UsageRecord.class))
                .collect(Collectors.toList());
    }

}
