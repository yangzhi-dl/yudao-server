package cn.iocoder.yudao.module.kafkalab.dal.mysql;

import cn.iocoder.yudao.module.kafkalab.dal.dataobject.ImbotGroupChatLogDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ImbotGroupChatLogMapper {
    @Insert("INSERT INTO imbot_group_chat_logs (message_uid, group_id, sender_id, content_text, source_platform, msg_type, msg_timestamp, summarize_status, extra) "
            + "VALUES (#{messageUid}, #{groupId}, #{senderId}, #{contentText}, #{sourcePlatform}, #{msgType}, #{msgTimestamp}, #{summarizeStatus}, #{extra})")
    int insert(ImbotGroupChatLogDO message);

    @Select("SELECT COUNT(1) FROM imbot_group_chat_logs")
    long count();
}
