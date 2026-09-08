package cn.iocoder.yudao.module.ai.common.model.vo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryStatus;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

@Data
@Builder /*@Builder可以让你类链式的调用你的代码，来初始化你的实例对象*/
@NoArgsConstructor /*注解在类上；为类提供一个无参的构造方法*/
@AllArgsConstructor /*注解在类上；为类提供一个全参的构造方法*/
@EqualsAndHashCode(callSuper = true)
public class TaskHistoryVO extends BaseDO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private String taskName;

    private TaskHistoryType taskType;

    private TaskHistoryStatus status;

}
