package cn.iocoder.yudao.module.ai.data.governance.controller.admin.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 归档资产到知识库请求 VO。
 */
@Data
public class AssetArchiveReqVO {

    @NotNull(message = "资产 ID 不能为空")
    private Long assetId;

    @NotNull(message = "知识库 ID 不能为空")
    private Long wikiId;

    @NotNull(message = "父目录 ID 不能为空")
    private Long parentId;

    @NotNull(message = "文档分类不能为空")
    private Long categoryId;

    @NotEmpty(message = "文档标签不能为空")
    private List<Long> tagIds;

    /**
     * 标题，为空时沿用资产标题。
     */
    private String title;

    private String summary;

    private Long cover;

    private Boolean isTop;

}
