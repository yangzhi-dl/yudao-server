package cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo;

import cn.iocoder.yudao.module.ai.common.enums.TaskStatusEnum;
import cn.iocoder.yudao.module.ai.knowledge.wiki.enums.WikiTypeEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserWikiPageListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 知识库 ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    /**
     * 知识库标题
     */
    private String title;

    private String imgUrl;

    private WikiTypeEnum type;

    private TaskStatusEnum taskStatus;

    private Boolean isTop;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 第一篇文档 ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long firstArticleId;

    /**
     * 创建时间
     */
    private LocalDateTime updateTime;

    /**
     * 当前用户对该资源拥有的 ACL 权限列表（如 READ / DELETE）
     */
    private List<String> permissions;

}
