package cn.iocoder.yudao.module.kafkalab.dal.dataobject;

import lombok.Data;

@Data
public class ImbotGroupChatLogDO {
    private Long id;
    private Long messageUid;
    private String groupId;
    private String senderId;
    private String contentText;
    private String sourcePlatform;
    private Integer msgType;
    private Long msgTimestamp;
    private Integer summarizeStatus;
    private String extra;
}
