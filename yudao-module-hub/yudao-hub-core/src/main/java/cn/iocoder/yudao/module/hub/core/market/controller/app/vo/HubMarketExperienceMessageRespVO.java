package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiAgentMessage;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiContent;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatTool;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.FileInfo;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.UserContent;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.DocMetadataVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户前台 - Hub 智能体体验消息响应对象")
@Data
public class HubMarketExperienceMessageRespVO {

    @Schema(description = "消息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Schema(description = "上一条消息编号", example = "199")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long lastId;

    @Schema(description = "消息发送方", requiredMode = Schema.RequiredMode.REQUIRED, example = "assistant")
    private String sender;

    @Schema(description = "普通智能体回答内容")
    private List<AiContent> aiContent;

    @Schema(description = "多智能体回答内容")
    private AiAgentMessage aiAgentMessage;

    @Schema(description = "用户提问内容")
    private UserContent userContent;

    @Schema(description = "引用文档信息")
    private List<DocMetadataVO> docMetadata;

    @Schema(description = "工具调用信息")
    private List<ChatTool> tools;

    @Schema(description = "消息附件信息")
    private List<FileInfo> fileInfos;

    @Schema(description = "是否收藏")
    private Boolean isStar;

    @Schema(description = "反馈状态")
    private Integer feedbackStatus;

}
