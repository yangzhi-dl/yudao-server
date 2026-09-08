package cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo;

import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindPreNextArticleVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindWikiDocumentPreNextVO {
    /**
     * 上一篇文档
     */
    private FindPreNextArticleVO preArticle;
    /**
     * 下一篇文档
     */
    private FindPreNextArticleVO nextArticle;

}