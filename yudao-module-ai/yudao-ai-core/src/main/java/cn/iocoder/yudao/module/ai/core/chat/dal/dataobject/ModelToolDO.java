package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.ToolType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolFunction;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolSettings;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * AI 工具 DO
 *
 * @author yudao
 */
@TableName(value = "ai_chat_tools", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelToolDO extends BaseDO {

    /**
     * 工具编号
     */
    @TableId
    private Long id;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具别名
     */
    @TableField("`rename`")
    private String rename;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 工具类型
     *
     * 枚举 {@link ToolType}
     */
    @TableField("`type`")
    private ToolType type;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 工具配置（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ToolSettings settings;

    /**
     * 工具方法列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ToolFunction> functions;

}
