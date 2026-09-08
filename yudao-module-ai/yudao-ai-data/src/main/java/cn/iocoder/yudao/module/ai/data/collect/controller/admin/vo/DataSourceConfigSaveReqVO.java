package cn.iocoder.yudao.module.ai.data.collect.controller.admin.vo;

import cn.iocoder.yudao.module.ai.data.collect.model.SourceConfigParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据源配置新增/更新请求 VO。
 */
@Data
public class DataSourceConfigSaveReqVO {

    private Long id;

    @NotNull(message = "采集源类型不能为空")
    private Integer sourceType;

    @NotBlank(message = "数据源名称不能为空")
    private String name;

    /**
     * 采集源配置参数（多态，type 字段对应 {@code DataSourceType} 枚举名）。
     */
    private SourceConfigParam config;

    private Boolean enabled;

    private String remark;

}
