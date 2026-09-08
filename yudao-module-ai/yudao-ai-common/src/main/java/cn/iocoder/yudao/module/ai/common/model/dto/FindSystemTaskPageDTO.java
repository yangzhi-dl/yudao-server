package cn.iocoder.yudao.module.ai.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindSystemTaskPageDTO {

    /**
     * 任务名称
     */
    private String taskName;

    // 页码
    private int page;

    // 每页显示记录数
    private int pageSize;

}
