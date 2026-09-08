package cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo;

import cn.iocoder.yudao.module.ai.common.model.entity.Tag;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindPreNextArticleVO;
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
public class FindWikiDocumentDetailVO {

    /**
     * 文档标题
     */
    private String title;

    private String summary;

    /**
     * 文档正文（HTML）
     */
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long fileId;

    /**
     * 发布时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 分类 ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long categoryId;
    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 阅读量
     */
    private Long readNum;
    /**
     * 标签集合
     */
    private List<Tag> tags;
    /**
     * 上一篇文档
     */
    private FindPreNextArticleVO preArticle;
    /**
     * 下一篇文档
     */
    private FindPreNextArticleVO nextArticle;

    /**
     * 总字数
     */
    private Integer totalWords;

    /**
     * 阅读时长
     */
    private String readTime;

    private List<String> permissions;
}
