package cn.iocoder.yudao.module.ai.knowledge.document.model.vo;

import cn.iocoder.yudao.module.ai.common.model.entity.BaseUserInfo;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotDocumentVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private String title;

    private String imageUrl;

    private String categoryName;

    private List<String> tagNames;

    private BaseUserInfo userInfo;

    private Long wordCount;

    private String summary;

    private LocalDateTime createTime;

}
