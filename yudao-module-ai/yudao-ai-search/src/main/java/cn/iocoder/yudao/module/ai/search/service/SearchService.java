package cn.iocoder.yudao.module.ai.search.service;

import cn.iocoder.yudao.module.ai.search.model.SearchResponse;

public interface SearchService {

    /**
     * 执行搜索
     * @param query 搜索关键词
     * @return 搜索结果
     */
    SearchResponse search(String query);

    /**
     * 获取服务类型
     */
    String getType();

    /**
     * 服务是否可用
     */
    boolean isAvailable();
}