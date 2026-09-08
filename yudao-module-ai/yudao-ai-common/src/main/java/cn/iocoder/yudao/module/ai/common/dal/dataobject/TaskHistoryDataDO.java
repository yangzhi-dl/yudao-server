package cn.iocoder.yudao.module.ai.common.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 任务历史数据 DO
 *
 * @author yudao
 */
@TableName("ai_task_history_data")
@KeySequence("ai_task_history_data_seq")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryDataDO {

    /**
     * 任务编号
     * <p>
     * 关联 {@link TaskHistoryDO#getId()}
     */
    @TableId
    private Long taskId;

    /**
     * 未处理数据
     */
    private String unprocessed;

    /**
     * 结果数据
     */
    private String result;

}
