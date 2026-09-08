package cn.iocoder.yudao.module.hub.core.portal.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Hub 门户内容类型枚举。
 */
@Getter
@AllArgsConstructor
public enum HubPortalContentTypeEnum implements ArrayValuable<String> {

    HERO("hero", "首屏"),
    SECTION("section", "页面分区"),
    CAPABILITY("capability", "平台能力"),
    PRODUCT("product", "产品"),
    PRODUCT_MEDIA("product_media", "产品素材"),
    SOLUTION("solution", "解决方案"),
    SOLUTION_MEDIA("solution_media", "解决方案素材"),
    CASE("case", "典型场景"),
    PRIVATE_DEPLOYMENT("private_deployment", "私有化部署"),
    CTA("cta", "行动区");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(HubPortalContentTypeEnum::getType)
            .toArray(String[]::new);

    private final String type;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
