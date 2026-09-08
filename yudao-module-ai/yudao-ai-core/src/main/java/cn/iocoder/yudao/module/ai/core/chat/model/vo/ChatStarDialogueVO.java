package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiAgentMessage;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiContent;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatTool;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.FileInfo;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatStarDialogueVO implements Serializable {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private List<AiContent> aiContent;

    private AiAgentMessage aiAgentMessage;

    private List<DocMetadataVO> docMetadata;

    private List<ChatTool> tools;

    private List<FileInfo> fileInfos;

}

