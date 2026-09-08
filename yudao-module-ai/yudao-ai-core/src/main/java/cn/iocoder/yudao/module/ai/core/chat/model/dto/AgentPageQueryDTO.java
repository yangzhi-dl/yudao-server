package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AgentPageQueryDTO {

    private String name;

    /**
     * 可访问的智能体 ID 集合（内部使用，用于 ACL 过滤后的数据库分页）
     */
    private List<Long> ids;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
