package cn.iocoder.yudao.module.ai.knowledge.document.model.vo;

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
public class FindDocumentPageListVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private String title;

    /**
     * 文档封面
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long cover;

    private String imageUrl;

    private String summary;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long categoryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private List<Long> tagIds;

    /**
     * 文档类型
     */
    private Integer type;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * ACL 权限字符列表
     */
    private List<String> permissions;

    /**
     * 是否为授权过来的内容（非当前用户创建）
     */
    private Boolean granted;

    private Integer weight;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
