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
public class BaiduSearchResponse {

    private List<SearchResponse.Result> references;

}
