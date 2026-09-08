package cn.iocoder.yudao.module.ai.knowledge.document.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveDocumentContentDTO {

    @NotNull(message = "文档 ID 不能为空")
    private Long id;

    private String content;

}
