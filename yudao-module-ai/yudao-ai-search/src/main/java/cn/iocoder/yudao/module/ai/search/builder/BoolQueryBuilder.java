package cn.iocoder.yudao.module.ai.search.builder;

import cn.iocoder.yudao.module.ai.search.model.FilterCondition;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bool Query 构建器 - 组装多个过滤条件
 * 注意：强制要求所有条件的 andRelation 保持一致，避免逻辑歧义
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoolQueryBuilder {

    private final FilterConditionParser parser;

    /**
     * 构建 BoolQuery
     * @param conditions 过滤条件列表
     * @return Query DSL
     */
    public Query build(List<FilterCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            // 无条件时匹配所有
            return co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery.of(m -> m)._toQuery();
        }

        Boolean firstRelation = getaBoolean(conditions);

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        boolean useAnd = Boolean.TRUE.equals(firstRelation); // true=AND, false=OR
        boolean hasShould = false; // 标记是否添加了 should 条件

        for (FilterCondition condition : conditions) {
            Query query = parser.parse(condition);
            if (query == null) {
                log.warn("过滤条件解析为空，已跳过: {}", condition);
                continue;
            }

            if (useAnd) {
                boolBuilder.must(query);
            } else {
                boolBuilder.should(query);
                hasShould = true; // 记录添加了 should 条件
            }
        }

        if (hasShould) {
            boolBuilder.minimumShouldMatch("1");
        }

        return boolBuilder.build()._toQuery();
    }

    private static Boolean getaBoolean(List<FilterCondition> conditions) {
        Boolean firstRelation = conditions.get(0).getAndRelation();
        for (int i = 1; i < conditions.size(); i++) {
            FilterCondition cond = conditions.get(i);
            if (firstRelation == null || !firstRelation.equals(cond.getAndRelation())) {
                throw new IllegalArgumentException(
                        String.format("条件关系不一致！所有条件的 andRelation 必须相同。" +
                                        "索引0: %s, 索引%d: %s",
                                firstRelation, i, cond.getAndRelation()));
            }
        }
        return firstRelation;
    }
}