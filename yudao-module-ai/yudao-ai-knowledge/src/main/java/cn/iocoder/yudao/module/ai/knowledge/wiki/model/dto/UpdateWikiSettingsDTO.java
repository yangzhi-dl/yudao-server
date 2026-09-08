package cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.ChunkMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWikiSettingsDTO {

    @NotNull
    private Long id;

    @NotNull
    private Boolean autoVectorize;

    @NotNull
    private ChunkMethod chunkMethod;

    @NotNull
    @Min(1000)
    @Max(3000)
    private Integer chunkSize;

    @NotNull
    @Min(0)
    @Max(300)
    private Integer overlapSize;
}
