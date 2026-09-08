package cn.iocoder.yudao.module.ai.search.model;

import cn.iocoder.yudao.module.ai.search.enums.FilterOperatorEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用过滤条件
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterCondition implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 字段名（支持嵌套字段，如：attributes.key）
     */
    @NotBlank(message = "字段名不能为空")
    private String field;
    
    /**
     * 操作符
     */
    @NotNull(message = "操作符不能为空")
    private FilterOperatorEnum operator;
    
    /**
     * 单个值（用于 EQ, GT, LT 等）
     */
    private Object value;
    
    /**
     * 多个值（用于 IN, NOT_IN）
     */
    private List<Object> values;
    
    /**
     * 范围起始值（用于 BETWEEN）
     */
    private Object startValue;
    
    /**
     * 范围结束值（用于 BETWEEN）
     */
    private Object endValue;
    
    /**
     * 是否启用（动态开关）
     */
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 逻辑关系（与上一个条件的关系）
     * true: AND, false: OR
     */
    @Builder.Default
    private Boolean andRelation = true;
}