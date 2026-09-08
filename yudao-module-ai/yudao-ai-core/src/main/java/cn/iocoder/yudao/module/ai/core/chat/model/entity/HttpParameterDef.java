package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * HTTP参数定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpParameterDef {
    
    /**
     * 参数名称（模型调用时使用的名称）
     */
    private String name;
    
    /**
     * 参数类型：string, integer, number, boolean, array, object
     */
    private String type;

    /**
     * 参数位置：query, path, body, header
     * 默认为 query
     */
    private String location = "query";
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 参数描述（给模型看的）
     */
    private String description;
    
    /**
     * 默认值
     */
    private Object defaultValue;
    
    /**
     * 枚举值列表
     */
    private List<String> enumValues;
    
    /**
     * 正则表达式校验（字符串类型）
     */
    private String pattern;

}