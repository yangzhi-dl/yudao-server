package cn.iocoder.yudao.module.ai.search.enums;

import lombok.Getter;

@Getter
public enum IndexNameEnum {

        DOCUMENT("iims-pro-document-metadata"),

        WIKI("iims-pro-wiki-metadata");

        private final String value;

        IndexNameEnum(String value) {
            this.value = value;
        }

}