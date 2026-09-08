package cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库权限 VO
 *
 * @author IIMS
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WikiPermissionVO {

    /** 权限记录ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    /** 主体类型 */
    private String principalType;

    /** 主体类型描述 */
    private String principalTypeDesc;

    /** 主体ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long principalId;

    /** 主体名称（解析后的用户姓名/角色名/部门名等） */
    private String principalName;

    /** 权限掩码值 */
    private Integer permissionMask;

    /** 拥有的权限列表（READ/WRITE/DELETE/MANAGE...） */
    private List<String> permissions;

    /** 授予时间 */
    private LocalDateTime grantTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 授予者名称 */
    private String grantByName;

    /** 备注 */
    private String remark;
}
