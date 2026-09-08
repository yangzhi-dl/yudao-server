package cn.iocoder.yudao.module.ai.common.model.entity;

import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import cn.iocoder.yudao.module.ai.common.enums.TaskScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder /*@Builder可以让你类链式的调用你的代码，来初始化你的实例对象*/
@NoArgsConstructor /*注解在类上；为类提供一个无参的构造方法*/
@AllArgsConstructor /*注解在类上；为类提供一个全参的构造方法*/
public class InitTask {

    private String taskName;

    private Long objectId;

    private TaskHistoryType taskType;

    private TaskScheduleType scheduleType;

}
