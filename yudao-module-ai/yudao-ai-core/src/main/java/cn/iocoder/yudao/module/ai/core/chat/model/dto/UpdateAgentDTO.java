package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.MultiAgentSettings;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAgentDTO {

    private Long id;

    private Long modelId;

    private Long categoryId;

    private List<Long> tagIds;

    private String name;

    private Long cover;

    private String prompt;

    private String description;

    private List<Long> skillIds;

    List<MultiAgentSettings> settings;

    private List<Long> tools;

}
