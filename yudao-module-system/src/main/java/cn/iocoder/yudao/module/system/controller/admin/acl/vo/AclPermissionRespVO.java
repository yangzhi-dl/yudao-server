package cn.iocoder.yudao.module.system.controller.admin.acl.vo;

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
public class AclPermissionRespVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String resourceType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long resourceId;

    private String principalType;

    private String principalTypeDesc;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long principalId;

    private String principalName;

    private Integer permissionMask;

    private List<String> permissions;

    private LocalDateTime grantTime;

    private LocalDateTime expireTime;

    private String grantByName;

    private String remark;
}