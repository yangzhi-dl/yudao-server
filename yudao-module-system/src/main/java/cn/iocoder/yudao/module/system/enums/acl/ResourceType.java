package cn.iocoder.yudao.module.system.enums.acl;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ResourceType {
    FILE(0, "文件"),
    WIKI(1, "知识库"),
    DOCUMENT(2, "文档"),
    AGENT(3, "智能体"),
    WORKFLOW(4, "工作流"),
    MODEL(5, "模型");
    
    @EnumValue
    private final int value;
    private final String description;
    
    ResourceType(int value, String description) {
        this.value = value;
        this.description = description;
    }
}

