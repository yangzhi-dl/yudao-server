package cn.iocoder.yudao.module.ai.common.model.dto;

import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryStatus;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryTaskHistoryDTO {

    private String taskName;

    private TaskHistoryStatus status;

    private TaskHistoryType taskType;

    private int page;

    private int pageSize;

}
