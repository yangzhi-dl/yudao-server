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
 * 采集结果 DO。
 */
@TableName("ai_data_collect_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectResult extends TenantBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 采集任务 ID。
     */
    private Long taskId;

    /**
     * 来源路径（文件路径或 URL）。
     */
    private String sourcePath;

    /**
     * 文件名。
     */
    private String fileName;

    /**
     * 文件类型（扩展名，小写）。
     */
    private String fileType;

    /**
     * 文件大小（字节）。
     */
    private Long fileSize;

    /**
     * 文件 SHA-256 摘要。
     */
    private String fileHash;

    /**
     * 采集产物（Markdown）归档文件 ID。
     */
    private Long warehouseFileId;

    /**
     * 关联的数据资产 ID。
     */
    private Long assetId;

    /**
     * 结果状态（{@link cn.iocoder.yudao.module.ai.data.collect.enums.CollectResultStatus#getValue()}）。
     */
    private Integer status;

    /**
     * 转换状态（{@link cn.iocoder.yudao.module.ai.data.collect.enums.ConvertStatus#getValue()}）。
     */
    private Integer convertStatus;

    /**
     * 错误信息。
     */
    private String errorMsg;

}
