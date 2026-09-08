package cn.iocoder.yudao.module.kafkalab.dal.mysql;

import cn.iocoder.yudao.module.kafkalab.dal.dataobject.ImbotTaskDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ImbotTaskMapper {
    @Insert("INSERT INTO imbot_task (group_id, group_name, biz_date, task_id, topic, status, is_claimed, evidence) "
            + "VALUES (#{groupId}, #{groupName}, #{bizDate}, #{taskId}, #{topic}, #{status}, #{isClaimed}, CAST(#{evidence} AS JSON)) "
            + "ON DUPLICATE KEY UPDATE topic = VALUES(topic), status = VALUES(status), evidence = VALUES(evidence), updated_at = CURRENT_TIMESTAMP")
    int upsert(ImbotTaskDO task);

    @Select("SELECT COUNT(1) FROM imbot_task")
    long count();
}
