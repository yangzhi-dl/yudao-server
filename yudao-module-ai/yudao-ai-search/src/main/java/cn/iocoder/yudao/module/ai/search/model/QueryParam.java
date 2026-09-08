package cn.iocoder.yudao.module.ai.search.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用查询参数
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryParam implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 过滤条件列表
     */
    @Valid
    private List<FilterCondition> filters;
    
    /**
     * 排序字段
     */
    private String sortField;
    
    /**
     * 排序方向：asc / desc
     */
    private String sortOrder = "desc";
    
    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer pageSize = 10;
    
    /**
     * 高亮字段
     */
    private List<String> highlightFields;
    
    /**
     * 是否返回总数
     */
    private Boolean returnTotal = true;
}