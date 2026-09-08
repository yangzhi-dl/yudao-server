package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.common.model.vo.UserInfoVO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private String rename;

    private String name;

    private String url;

    private String token;

    private String description;

    private AiApiType type;

    private AiModelType modelType;

    private Boolean isOnline;

    private LocalDateTime detectionTime;

    private List<UserInfoVO> users;

    /**
     * ACL 权限字符列表
     */
    private List<String> permissions;

    /**
     * 是否为授权过来的内容（非当前用户创建）
     */
    private Boolean granted;

}
