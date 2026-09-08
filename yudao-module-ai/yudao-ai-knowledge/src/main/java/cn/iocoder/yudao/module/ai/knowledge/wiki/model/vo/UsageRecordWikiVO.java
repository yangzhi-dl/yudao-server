package cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo;

import cn.iocoder.yudao.module.ai.knowledge.wiki.enums.WikiTypeEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsageRecordWikiVO {

    /**
     * 知识库 ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long firstArticleId;

    /**
     * 知识库标题
     */
    private String title;

    private String imgUrl;

    private WikiTypeEnum type;

    /**
     * 摘要
     */
    private String summary;

    private LocalDateTime usageTime;

}
