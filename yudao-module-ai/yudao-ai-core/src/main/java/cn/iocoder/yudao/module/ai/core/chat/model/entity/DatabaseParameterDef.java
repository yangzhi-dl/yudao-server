package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据库语句参数定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseParameterDef {

    private String name;

    private String type;

    private String description;

    private Boolean required;

    private Object defaultValue;

    private List<String> enumValues;
}