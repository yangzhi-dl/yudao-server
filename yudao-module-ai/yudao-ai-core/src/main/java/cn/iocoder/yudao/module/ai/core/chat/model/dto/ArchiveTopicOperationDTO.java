package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArchiveTopicOperationDTO {

    private List<Long> ids;

    private Boolean isArchived;

}
