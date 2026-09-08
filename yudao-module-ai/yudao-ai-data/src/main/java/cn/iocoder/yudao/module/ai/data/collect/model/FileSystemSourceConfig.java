package cn.iocoder.yudao.module.ai.data.collect.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件系统采集源配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileSystemSourceConfig extends SourceConfigParam {

    /**
     * 要扫描的目录绝对路径。
     */
    private String directory;

    /**
     * 是否递归扫描子目录。
     */
    private Boolean recursive = true;

}
