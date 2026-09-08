package cn.iocoder.yudao.module.ai.data.collect.dal.dataobject;

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
 * 采集任务 DO。
 */
@TableName("ai_data_collect_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectTask extends TenantBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 采集源配置 ID。
     */
    private Long sourceConfigId;

    /**
     * 任务状态（{@link cn.iocoder.yudao.module.ai.data.collect.enums.CollectTaskStatus#getValue()}）。
     */
    private Integer status;

    /**
     * 采集文件总数。
     */
    private Integer totalCount;

    /**
     * 成功数。
     */
    private Integer successCount;

    /**
     * 失败数。
     */
    private Integer failCount;

    /**
     * 备注。
     */
    private String remark;

}
