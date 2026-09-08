package cn.iocoder.yudao.module.ai.search.builder;

import cn.iocoder.yudao.module.ai.search.enums.FilterOperatorEnum;
import cn.iocoder.yudao.module.ai.search.model.FilterCondition;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 过滤条件解析器 - 将 FilterCondition 转换为 Query DSL
 * 适配 Elasticsearch 8.x Java API Client
 */
@Slf4j
@Component
public class FilterConditionParser {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 解析单个过滤条件为 Query
     */
    public Query parse(FilterCondition condition) {
        if (condition == null || !Boolean.TRUE.equals(condition.getEnabled())) {
            return null;
        }

        String field = condition.getField();
        FilterOperatorEnum operator = condition.getOperator();

        try {
            return switch (operator) {
                case EQ -> buildTermQuery(field, condition.getValue());
                case NE -> BoolQuery.of(b -> b
                        .mustNot(buildTermQuery(field, condition.getValue()))
                )._toQuery();
                case GT -> buildRangeQuery(field, condition.getValue(), null, false, false);
                case GTE -> buildRangeQuery(field, condition.getValue(), null, true, false);
                case LT -> buildRangeQuery(field, null, condition.getValue(), false, false);
                case LTE -> buildRangeQuery(field, null, condition.getValue(), false, true);
                case IN -> buildTermsQuery(field, condition.getValues());
                case NOT_IN -> BoolQuery.of(b -> b
                        .mustNot(buildTermsQuery(field, condition.getValues()))
                )._toQuery();
                case BETWEEN -> buildRangeQuery(field,
                        condition.getStartValue(),
                        condition.getEndValue(),
                        true, true);
                case LIKE -> buildWildcardQuery(field, "*" + condition.getValue() + "*");
                case STARTS_WITH -> buildWildcardQuery(field, condition.getValue() + "*");
                case ENDS_WITH -> buildWildcardQuery(field, "*" + condition.getValue());
                case EXISTS -> ExistsQuery.of(e -> e.field(field))._toQuery();
                case NOT_EXISTS -> BoolQuery.of(b -> b
                        .mustNot(ExistsQuery.of(e -> e.field(field))._toQuery())
                )._toQuery();
                case MATCH -> buildMatchQuery(field, condition.getValue());
                case MATCH_PHRASE -> buildMatchPhraseQuery(field, condition.getValue());
            };
        } catch (Exception e) {
            log.error("解析过滤条件失败: {}", condition, e);
            return null;
        }
    }

    // ===== 私有辅助方法（修复版） =====

    private Query buildTermQuery(String field, Object value) {
        if (value == null) return null;

        // 使用 FieldValue 包装值（8.x 必需）
        FieldValue fieldValue = convertToFieldValue(value);

        return TermQuery.of(t -> t
                .field(field)
                .value(fieldValue)
        )._toQuery();
    }

    private Query buildTermsQuery(String field, List<Object> values) {
        if (values == null || values.isEmpty()) return null;

        // 转换为 FieldValue 列表（8.x 必需）
        List<FieldValue> fieldValues = values.stream()
                .filter(Objects::nonNull)
                .map(this::convertToFieldValue)
                .toList();

        return TermsQuery.of(t -> t
                .field(field)
                .terms(tq -> tq.value(fieldValues))
        )._toQuery();
    }

    private Query buildRangeQuery(String field, Object gte, Object lte,
                                  boolean includeGte, boolean includeLte) {
        return RangeQuery.of(r -> r
                .untyped(ur -> {
                    ur.field(field);

                    if (gte != null) {
                        JsonData gteData = JsonData.of(formatDateValue(gte));
                        if (includeGte) {
                            ur.gte(gteData);
                        } else {
                            ur.gt(gteData);
                        }
                    }

                    if (lte != null) {
                        JsonData lteData = JsonData.of(formatDateValue(lte));
                        if (includeLte) {
                            ur.lte(lteData);
                        } else {
                            ur.lt(lteData);
                        }
                    }

                    return ur;
                })
        )._toQuery();
    }

    private Query buildWildcardQuery(String field, String value) {
        if (!StringUtils.hasText(value)) return null;
        return WildcardQuery.of(w -> w
                .field(field)
                .value(value)
        )._toQuery();
    }

    private Query buildMatchQuery(String field, Object value) {
        if (value == null || !StringUtils.hasText(value.toString())) return null;
        return MatchQuery.of(m -> m
                .field(field)
                .query(value.toString())
        )._toQuery();
    }

    private Query buildMatchPhraseQuery(String field, Object value) {
        if (value == null || !StringUtils.hasText(value.toString())) return null;
        return MatchPhraseQuery.of(m -> m
                .field(field)
                .query(value.toString())
        )._toQuery();
    }

    // ===== 工具方法 =====

    /**
     * 将任意类型值转换为 FieldValue（8.x 必需）
     */
    private FieldValue convertToFieldValue(Object value) {
        if (value == null) {
            return FieldValue.NULL;
        } else if (value instanceof String) {
            return FieldValue.of((String) value);
        } else if (value instanceof Integer) {
            return FieldValue.of((Integer) value);
        } else if (value instanceof Long) {
            return FieldValue.of((Long) value);
        } else if (value instanceof Boolean) {
            return FieldValue.of((Boolean) value);
        } else if (value instanceof Double) {
            return FieldValue.of((Double) value);
        } else if (value instanceof Float) {
            return FieldValue.of((Float) value);
        } else {
            // 兜底：转为字符串
            return FieldValue.of(value.toString());
        }
    }

    /**
     * 格式化日期值为字符串
     */
    private String formatDateValue(Object value) {
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_TIME_FORMATTER);
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DATE_FORMATTER);
        } else if (value instanceof Date) {
            return DATE_TIME_FORMATTER.format(((Date) value).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        return value.toString();
    }
}