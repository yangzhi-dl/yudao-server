package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long modelId;

    private String modelName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long cover;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    private String categoryName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tagIds;

    private List<String> tagNames;

    private String imageUrl;

    private AiModelType modelType;

    private AgentType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> skillIds;

    private String description;

    private LocalDateTime createTime;

    /**
     * ACL 权限字符列表
     */
    private List<String> permissions;

    /**
     * 是否为授权过来的内容（非当前用户创建）
     */
    private Boolean granted;

}
