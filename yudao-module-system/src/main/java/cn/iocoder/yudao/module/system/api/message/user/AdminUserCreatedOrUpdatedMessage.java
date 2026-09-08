package cn.iocoder.yudao.module.system.api.message.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 管理员用户新增或编辑消息。
 *
 * <p>可选业务模块可订阅此消息，补齐该用户所需的模块默认配置。</p>
 */
@Data
@Accessors(chain = true)
public class AdminUserCreatedOrUpdatedMessage {

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

}
