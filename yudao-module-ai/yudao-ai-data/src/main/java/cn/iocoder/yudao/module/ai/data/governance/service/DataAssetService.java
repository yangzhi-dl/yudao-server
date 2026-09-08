package cn.iocoder.yudao.module.ai.data.governance.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.DataAsset;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.GovernanceRecord;

import java.util.List;

/**
 * 数据资产与治理 Service。
 */
public interface DataAssetService {

    /**
     * 根据采集产物创建数据资产（状态：待治理）。
     *
     * @return 资产 ID
     */
    Long createAsset(Long sourceConfigId, Long collectTaskId, Long collectResultId,
                     Long warehouseFileId, String title, String markdown);

    /**
     * 查询数据资产。
     */
    DataAsset getAsset(Long id);

    /**
     * 分页查询数据资产。
     */
    PageResult<DataAsset> getAssetPage(PageParam pageParam, Integer status, String title);

    /**
     * 查询数据资产 Markdown 正文。
     */
    String getAssetContent(Long assetId);

    /**
     * 人工编辑资产（标题/摘要/正文），编辑后进入「已治理」状态。
     */
    void editAsset(Long assetId, String title, String summary, String content);

    /**
     * AI 清洗资产，清洗后进入「已治理」状态。
     *
     * @param prompt 自定义提示词，为空时使用默认提示词
     */
    void aiCleanAsset(Long assetId, String prompt);

    /**
     * 废弃资产。
     */
    void discardAsset(Long assetId);

    /**
     * 归档资产到知识库文档。
     */
    void archiveAsset(Long assetId, Long wikiId, Long parentId, Long categoryId,
                      List<Long> tagIds, String title, String summary, Long cover, Boolean isTop);

    /**
     * 查询资产的治理记录。
     */
    List<GovernanceRecord> getGovernanceRecords(Long assetId);

}
