package cn.iocoder.yudao.module.ai.common.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryStatus;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import cn.iocoder.yudao.module.ai.common.enums.TaskScheduleType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 任务历史 DO
 *
 * @author yudao
 */
@TableName("ai_task_history")
@KeySequence("ai_task_history_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 关联对象编号
     */
    private Long objectId;

    /**
     * 任务类型
     *
     * 枚举 {@link TaskHistoryType}
     */
    private TaskHistoryType taskType;

    /**
     * 调度类型
     *
     * 枚举 {@link TaskScheduleType}
     */
    private TaskScheduleType scheduleType;

    /**
     * 任务状态
     *
     * 枚举 {@link TaskHistoryStatus}
     */
    @TableField("`status`")
    private TaskHistoryStatus status;

}
