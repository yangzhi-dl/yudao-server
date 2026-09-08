package cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.enums.WikiTypeEnum;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiSettings;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;

@TableName(value = "ai_wiki", autoResultMap = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Wiki extends TenantBaseDO implements Serializable {

    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long cover;

    private String summary;

    private Boolean deleted;

    private Integer weight;

    private WikiTypeEnum type;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private WikiSettings settings;

}
