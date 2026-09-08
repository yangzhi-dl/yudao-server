package cn.iocoder.yudao.module.ai.knowledge.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentTypeEnum {

    NORMAL(1, "普通"),
    WIKI(2, "收录于知识库");

    private final Integer value;
    private final String description;

}
