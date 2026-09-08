package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelConfig;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * AI 模型设置 DO
 *
 * @author yudao
 */
@TableName(value = "ai_chat_settings", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelSettingDO extends BaseDO {

    /**
     * 用户ID（主键）
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 设置信息（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ModelConfig> configs;

    /**
     * 获取 LANGUAGE 类型的模型 ID（向后兼容）
     */
    public Long getLanguageModelId() {
        return getModelIdByType(AiModelType.LANGUAGE);
    }

    /**
     * 获取 EMBEDDING 类型的模型 ID（向后兼容）
     */
    public Long getEmbeddingModel() {
        return getModelIdByType(AiModelType.EMBEDDING);
    }

    private Long getModelIdByType(AiModelType type) {
        if (configs == null) {
            return null;
        }
        return configs.stream()
                .filter(c -> c.getType() == type)
                .findFirst()
                .map(ModelConfig::getId)
                .orElse(null);
    }

}
