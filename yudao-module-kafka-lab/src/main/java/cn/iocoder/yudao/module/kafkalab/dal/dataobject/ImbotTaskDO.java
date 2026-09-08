package cn.iocoder.yudao.module.kafkalab.dal.dataobject;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ImbotTaskDO {
    private String groupId;
    private String groupName;
    private LocalDate bizDate;
    private String taskId;
    private String topic;
    private String status;
    private Integer isClaimed;
    private String evidence;
}
