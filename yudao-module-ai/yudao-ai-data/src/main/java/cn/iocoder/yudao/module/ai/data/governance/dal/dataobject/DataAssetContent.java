package cn.iocoder.yudao.module.ai.data.governance.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 数据资产正文 DO（Markdown）。
 */
@TableName("ai_data_asset_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataAssetContent extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 数据资产 ID。
     */
    private Long assetId;

    /**
     * Markdown 正文。
     */
    private String content;

    /**
     * 最近一次 AI 清洗使用的提示词。
     */
    private String prompt;

}
