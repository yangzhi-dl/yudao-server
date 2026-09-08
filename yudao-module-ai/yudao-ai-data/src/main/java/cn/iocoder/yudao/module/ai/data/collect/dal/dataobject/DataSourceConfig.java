package cn.iocoder.yudao.module.ai.data.collect.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.data.collect.model.SourceConfigParam;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据源配置 DO。
 */
@TableName(value = "ai_data_source_config", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataSourceConfig extends TenantBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 采集源类型（{@link cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType#getValue()}）。
     */
    private Integer sourceType;

    /**
     * 数据源名称。
     */
    private String name;

    /**
     * 采集源配置参数（JSON，多态）。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private SourceConfigParam config;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 备注。
     */
    private String remark;

}
