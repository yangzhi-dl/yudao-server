package cn.iocoder.yudao.module.hub.core.market.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Hub 市场发布状态。
 */
@Getter
@AllArgsConstructor
public enum HubMarketPublishStatusEnum implements ArrayValuable<Integer> {

    UNPUBLISHED(0, "未发布"),
    PUBLISHED(1, "已发布"),
    OFF_SHELF(2, "已下架");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HubMarketPublishStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
