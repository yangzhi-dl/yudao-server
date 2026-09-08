package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 模型 DO
 *
 * @author yudao
 */
@TableName("ai_chat_models")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelDO extends TenantBaseDO {

    /**
     * 模型编号
     */
    @TableId
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型别名
     */
    @TableField("`rename`")
    private String rename;

    /**
     * API 类型
     *
     * 枚举 {@link AiApiType}
     */
    @TableField("`type`")
    private AiApiType type;

    /**
     * 模型类型
     *
     * 枚举 {@link AiModelType}
     */
    private AiModelType modelType;

    /**
     * API 地址
     */
    private String url;

    /**
     * API Key
     */
    @TableField("`key`")
    private String key;

    /**
     * Token 限制
     */
    private Long token;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 探测时间
     */
    private LocalDateTime detectionTime;

}
