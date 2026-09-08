package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.MultiAgentSettings;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateAgentDTO extends BaseDO {

    private Long modelId;

    private String name;

    private Long cover;

    private Long categoryId;

    private List<Long> tagIds;

    private AgentType type;

    private String prompt;

    private String description;

    private List<Long> skillIds;

    private List<MultiAgentSettings> settings;

    private List<Long> tools;

}