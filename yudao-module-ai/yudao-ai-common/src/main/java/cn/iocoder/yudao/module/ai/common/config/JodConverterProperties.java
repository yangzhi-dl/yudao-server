package cn.iocoder.yudao.module.ai.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JODConverter 本地转换配置属性
 * <p>
 * 对应 application.yaml 中的 jodconverter.local 配置，
 * 替代 jodconverter-spring-boot-starter 的自动配置。
 */
@Data
@ConfigurationProperties(prefix = "jodconverter.local")
public class JodConverterProperties {

    /** 是否启用本地转换 */
    private boolean enabled = true;

    /** LibreOffice 安装路径 */
    private String officeHome;

    /** 工作端口（多个端口实现并发） */
    private int[] portNumbers = {2002};

    /** 单个进程最大任务数 */
    private int maxTasksPerProcess = 200;

    /** 任务执行超时时间（毫秒） */
    private long taskExecutionTimeout = 120000L;

    /** 任务队列超时时间（毫秒） */
    private long taskQueueTimeout = 120000L;

    /** 进程超时时间（毫秒） */
    private long processTimeout = 120000L;

}
