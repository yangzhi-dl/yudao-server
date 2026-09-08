package cn.iocoder.yudao.module.ai.common.model.entity;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UsageRecord extends BaseDO implements Serializable {

    private Long id;

    private UsageRecordType type;

    private Long objectId;

}
