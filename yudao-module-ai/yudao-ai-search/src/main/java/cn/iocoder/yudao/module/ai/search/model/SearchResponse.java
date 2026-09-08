package cn.iocoder.yudao.module.ai.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    private String query;

    private List<Result> results;

    @Data
    public static class Result {
        private String title;
        private String url;
        private String content;
        private Double score;
    }
}