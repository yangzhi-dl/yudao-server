package cn.iocoder.yudao.module.kafkalab.dal.mysql;

import cn.iocoder.yudao.module.kafkalab.dal.dataobject.ImbotGroupDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ImbotGroupMapper {
    @Insert("INSERT INTO imbot_group (platform, group_id, group_name, last_message_at) VALUES (#{platform}, #{groupId}, #{groupName}, #{lastMessageAt}) "
            + "ON DUPLICATE KEY UPDATE group_name = VALUES(group_name), last_message_at = VALUES(last_message_at), updated_at = CURRENT_TIMESTAMP")
    int upsert(ImbotGroupDO group);

    @Select("SELECT COUNT(1) FROM imbot_group")
    long count();
}
