package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型为中心「设置用户默认模型」的结果
 *
 * <p>changed 取值：
 * <ul>
 *     <li>ADDED —— 该用户该类型无默认模型，已新增成功</li>
 *     <li>REPLACED —— 已强制替换为当前模型</li>
 *     <li>NOOP —— 该用户该类型默认模型已是当前模型，无需变更</li>
 *     <li>CONFLICT —— 该用户该类型已有其他默认模型，需前端确认后以 force 重试</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模型为中心设置用户默认模型结果")
public class AssignModelResultVO {

    public static final String CHANGED_ADDED = "ADDED";
    public static final String CHANGED_REPLACED = "REPLACED";
    public static final String CHANGED_NOOP = "NOOP";
    public static final String CHANGED_CONFLICT = "CONFLICT";

    @Schema(description = "变更结果：ADDED/REPLACED/NOOP/CONFLICT")
    private String changed;

    @Schema(description = "冲突时当前已设置的模型编号")
    private Long currentModelId;

    @Schema(description = "冲突时当前已设置的模型名称")
    private String currentModelName;
}