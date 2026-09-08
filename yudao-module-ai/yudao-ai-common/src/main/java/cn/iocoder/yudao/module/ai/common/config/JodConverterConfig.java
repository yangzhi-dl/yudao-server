package cn.iocoder.yudao.module.ai.common.config;

import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JODConverter 手动配置（替代 jodconverter-spring-boot-starter）
 * <p>
 * 使用 jodconverter-local 自行创建 DocumentConverter Bean，
 * 通过 {@link JodConverterProperties} 读取 yaml 配置。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(JodConverterProperties.class)
public class JodConverterConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "jodconverter.local", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LocalOfficeManager localOfficeManager(JodConverterProperties properties) {
        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .officeHome(properties.getOfficeHome())
                .portNumbers(properties.getPortNumbers())
                .maxTasksPerProcess(properties.getMaxTasksPerProcess())
                .taskExecutionTimeout(properties.getTaskExecutionTimeout())
                .taskQueueTimeout(properties.getTaskQueueTimeout())
                .processTimeout(properties.getProcessTimeout())
                .build();
        log.info("JODConverter LocalOfficeManager 已构建, officeHome={}, ports={}",
                properties.getOfficeHome(), properties.getPortNumbers());
        return officeManager;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "jodconverter.local", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DocumentConverter documentConverter(LocalOfficeManager localOfficeManager, JodConverterProperties properties) {
        LocalConverter converter = LocalConverter.builder()
                .officeManager(localOfficeManager)
                .build();
        log.info("JODConverter DocumentConverter 初始化完成");
        return converter;
    }

}
