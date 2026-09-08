package cn.iocoder.yudao.module.ai.common.enums;

import lombok.Getter;

@Getter
public enum CoreDictType {
    DOCUMENT_CATEGORY("ai_document_category"),
    DOCUMENT_TAG("ai_document_tag"),
    AGENT_CATEGORY("ai_agent_category"),
    AGENT_TAG("ai_agent_tag"),
    WORKFLOW_CATEGORY("ai_workflow_category"),
    WORKFLOW_TAG("ai_workflow_tag");

    private final String value;

    CoreDictType(String value) {
        this.value = value;
    }
}