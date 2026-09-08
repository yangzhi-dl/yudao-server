package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.core.rag.model.entity.Metadata;
import cn.iocoder.yudao.module.ai.core.rag.model.entity.MetadataScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocMetadataVO implements Serializable {

    private List<MetadataScore> score;

    private Metadata metadata;

}
