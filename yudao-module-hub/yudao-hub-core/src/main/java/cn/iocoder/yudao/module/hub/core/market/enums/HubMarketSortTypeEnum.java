package cn.iocoder.yudao.module.hub.core.market.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Hub 市场排序方式。
 */
@Getter
@AllArgsConstructor
public enum HubMarketSortTypeEnum implements ArrayValuable<Integer> {

    COMPREHENSIVE(0, "综合排序"),
    LATEST(1, "最新发布");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HubMarketSortTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
