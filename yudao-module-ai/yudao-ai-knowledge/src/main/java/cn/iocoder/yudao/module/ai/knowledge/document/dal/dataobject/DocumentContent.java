package cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@TableName(value = "ai_document_content", autoResultMap = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DocumentContent extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long documentId;

    private String content;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> chunkKeys;

}
