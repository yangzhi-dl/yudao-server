package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionAppVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private Long cover;

    private String name;

    private String imageUrl;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long usageCount;

    private Long categoryId;

    private String categoryName;

    private List<Long> tagIds;

    private List<String> tagNames;

    private LocalDateTime createTime;

}

