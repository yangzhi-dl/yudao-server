package cn.iocoder.yudao.module.ai.knowledge.document.model.vo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.common.model.entity.Tag;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.List;

/**
 * @Author: Aitenry
 * @Date: 2025/10/25 12:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class FindDocumentInfoDetailVO extends BaseDO {
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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long wikiId;

    private Integer type;
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
