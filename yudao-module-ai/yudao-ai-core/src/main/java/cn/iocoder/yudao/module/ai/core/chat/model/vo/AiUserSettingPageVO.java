package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI 用户设置状态")
public class AiUserSettingPageVO {

    @Schema(description = "用户编号")
    private Long id;
    @Schema(description = "用户账号")
    private String username;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "用户头像")
    private String avatar;
    @Schema(description = "组织名称")
    private String deptName;
    @Schema(description = "手机号码")
    private String mobile;
    @Schema(description = "是否已配置模型")
    private Boolean configured;
}
