package cn.iocoder.yudao.module.ai.data.governance.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 数据资产 DO。
 */
@TableName(value = "ai_data_asset", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataAsset extends TenantBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 采集源配置 ID。
     */
    private Long sourceConfigId;

    /**
     * 采集任务 ID。
     */
    private Long collectTaskId;

    /**
     * 采集结果 ID。
     */
    private Long collectResultId;

    /**
     * 采集产物（Markdown）归档文件 ID。
     */
    private Long fileId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 摘要。
     */
    private String summary;

    /**
     * 封面文件 ID。
     */
    private Long cover;

    /**
     * 分类 ID（归档到知识库时使用）。
     */
    private Long categoryId;

    /**
     * 标签 ID 列表（归档到知识库时使用）。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> tagIds;

    /**
     * 自动质量评分（0-100）。
     */
    private Integer qualityScore;

    /**
     * 资产状态（{@link cn.iocoder.yudao.module.ai.data.governance.enums.AssetStatus#getValue()}）。
     */
    private Integer status;

    /**
     * 归档后的知识库文档 ID。
     */
    private Long archivedDocumentId;

    /**
     * 归档后的知识库 ID。
     */
    private Long archivedWikiId;

    /**
     * 归档后的知识库目录 ID。
     */
    private Long archivedCatalogId;

}
