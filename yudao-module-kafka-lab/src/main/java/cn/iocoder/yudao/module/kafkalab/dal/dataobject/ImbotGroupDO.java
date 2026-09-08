package cn.iocoder.yudao.module.kafkalab.dal.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImbotGroupDO {
    private String platform;
    private String groupId;
    private String groupName;
    private LocalDateTime lastMessageAt;
}
