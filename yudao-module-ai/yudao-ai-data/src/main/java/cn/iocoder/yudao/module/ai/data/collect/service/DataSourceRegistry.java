package cn.iocoder.yudao.module.ai.data.collect.service;

import cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 采集源适配器注册表。
 * <p>
 * 启动时收集所有 {@link DataSource} 实现，按类型索引。
 */
@Component
public class DataSourceRegistry {

    private final Map<DataSourceType, DataSource> registry = new EnumMap<>(DataSourceType.class);

    public DataSourceRegistry(List<DataSource> dataSources) {
        for (DataSource dataSource : dataSources) {
            registry.put(dataSource.type(), dataSource);
        }
    }

    public DataSource get(DataSourceType type) {
        return registry.get(type);
    }

}
