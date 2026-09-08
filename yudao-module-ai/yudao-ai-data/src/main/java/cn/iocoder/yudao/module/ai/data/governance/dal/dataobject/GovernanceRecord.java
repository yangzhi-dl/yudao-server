package cn.iocoder.yudao.module.ai.data.governance.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
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
 * 数据治理记录 DO。
 */
@TableName("ai_data_governance_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GovernanceRecord extends TenantBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 数据资产 ID。
     */
    private Long assetId;

    /**
     * 治理动作（{@link cn.iocoder.yudao.module.ai.data.governance.enums.GovernanceAction#getValue()}）。
     */
    private Integer action;

    /**
     * 治理前正文。
     */
    private String contentBefore;

    /**
     * 治理后正文。
     */
    private String contentAfter;

    /**
     * 使用的提示词（AI 清洗时）。
     */
    private String prompt;

    /**
     * 操作人 ID。
     */
    private Long operatorId;

    /**
     * 备注。
     */
    private String remark;

}
