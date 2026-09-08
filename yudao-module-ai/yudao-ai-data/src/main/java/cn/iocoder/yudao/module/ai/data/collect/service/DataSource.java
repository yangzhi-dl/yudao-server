package cn.iocoder.yudao.module.ai.data.collect.service;

import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType;
import cn.iocoder.yudao.module.ai.data.collect.model.SourceFile;

import java.util.List;

/**
 * 采集源适配器接口（策略模式）。
 * <p>
 * 每种采集源类型提供一个实现，通过 {@link DataSourceRegistry} 注册。
 */
public interface DataSource {

    /**
     * 支持的采集源类型。
     */
    DataSourceType type();

    /**
     * 拉取采集产物。
     *
     * @param config 数据源配置
     * @return 采集到的源文件列表
     */
    List<SourceFile> fetch(DataSourceConfig config) throws Exception;

}
