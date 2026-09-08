package cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@TableName(value = "ai_document", autoResultMap = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Document extends TenantBaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String title;

    private Long cover;

    private String summary;

    private Long readNum;

    private Integer weight;

    private Integer type;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> tagIds;

    private Long categoryId;

    private Long fileId;

    /**
     * 内容资源文件 ID 列表（文章正文引用的图片/视频/音频/附件），
     * 保存文档内容时解析 ai_document_content 更新，用于文件访问权限关联
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> resourceIds;

}
