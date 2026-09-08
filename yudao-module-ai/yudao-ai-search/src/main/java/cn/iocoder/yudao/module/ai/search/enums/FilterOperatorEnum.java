package cn.iocoder.yudao.module.ai.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 过滤操作符
 */
@Getter
@AllArgsConstructor
public enum FilterOperatorEnum {
    
    EQ("eq", "等于"),
    NE("ne", "不等于"),
    GT("gt", "大于"),
    GTE("gte", "大于等于"),
    LT("lt", "小于"),
    LTE("lte", "小于等于"),
    
    LIKE("like", "模糊匹配"),
    STARTS_WITH("startsWith", "前缀匹配"),
    ENDS_WITH("endsWith", "后缀匹配"),
    
    IN("in", "在...之中"),
    NOT_IN("notIn", "不在...之中"),
    
    BETWEEN("between", "区间"),
    EXISTS("exists", "存在"),
    NOT_EXISTS("notExists", "不存在"),
    
    // 全文检索
    MATCH("match", "全文匹配"),
    MATCH_PHRASE("matchPhrase", "短语匹配");
    
    private final String code;
    private final String desc;
}