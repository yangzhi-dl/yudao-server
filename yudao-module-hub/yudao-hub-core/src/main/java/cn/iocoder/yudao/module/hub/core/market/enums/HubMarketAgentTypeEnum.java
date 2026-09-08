package cn.iocoder.yudao.module.hub.core.market.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Hub 市场智能体类型。
 */
@Getter
@AllArgsConstructor
public enum HubMarketAgentTypeEnum implements ArrayValuable<Integer> {

    SINGLE(0, "单智能体"),
    MULTI(1, "多智能体");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HubMarketAgentTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
