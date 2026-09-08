package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkillPageQueryDTO extends PageParam {

    private String name;

    private Integer status;

    private List<Long> filterIds;

}