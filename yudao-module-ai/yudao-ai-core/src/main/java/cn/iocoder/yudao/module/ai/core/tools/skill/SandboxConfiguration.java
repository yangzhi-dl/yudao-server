package cn.iocoder.yudao.module.ai.core.tools.skill;

import io.agentscope.runtime.sandbox.manager.ManagerConfig;
import io.agentscope.runtime.sandbox.manager.SandboxService;
import io.agentscope.runtime.sandbox.manager.client.container.BaseClientStarter;
import io.agentscope.runtime.sandbox.manager.client.container.docker.DockerClientStarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * 沙箱配置
 * <p>
 * 创建 SandboxService Bean，为技能脚本执行提供 Docker 隔离沙箱环境。
 * 通过 {@code ai.sandbox.enabled} 配置项控制是否启用（默认启用）。
 * 若 Docker 不可用，沙箱启动将失败，SkillWorkspaceTool 会自动回退到 ProcessBuilder 模式。
 */
@Slf4j
@Configuration
public class SandboxConfiguration {

    @Value("${iims.sandbox.uri:}")
    private String baseUrl;

    @Value("${iims.sandbox.host:localhost}")
    private String host;

    @Value("${iims.sandbox.port:2375}")
    private Integer port;

    @Value("${iims.sandbox.cert-path:}")
    private String certPath;

    private final StringRedisTemplate redisTemplate;

    public SandboxConfiguration(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean(initMethod = "start", destroyMethod = "cleanupAllSandboxes")
    @ConditionalOnProperty(name = "ai.sandbox.enabled", havingValue = "true", matchIfMissing = true)
    public SandboxService sandboxService() {
        log.info("正在初始化沙箱服务 (SandboxService)...");
        DockerClientStarter.Builder dockerBuilder = DockerClientStarter.builder().host(host).port(port);
        if (StringUtils.hasText(certPath)) {
            dockerBuilder.certPath(certPath); // 配置了证书目录即启用 Docker TLS
            // docker-java 仅在 DOCKER_TLS_VERIFY=true 时才会基于 cert-path 构建 TLS（SSLConfig），
            // 否则会以明文连接 stunnel 的 TLS 端口而失败。这里显式开启该 JVM 系统属性。
            System.setProperty("DOCKER_TLS_VERIFY", "1");
            log.info("沙箱将以 TLS 方式连接 Docker: tcp://{}:{}", host, port);
        }
        // 跨主机场景：库默认把沙箱容器连接地址 IP 硬编码为 localhost，应用与 Docker 不在同一台机器时
        // 无法连通。这里用 RemoteDockerClient 把 IP 替换为 Docker 所在主机的地址（host）。
        BaseClientStarter clientStarter = new RemoteDockerClientStarter(dockerBuilder.build(), host);
        ManagerConfig config = ManagerConfig.builder()
                .baseUrl(baseUrl).clientStarter(clientStarter)
                // 用 Redis 共享注册表替换库默认的进程内注册表：重启 / 多实例共用同一份
                // userId->容器 映射，库的去重逻辑会直接复用已有容器，避免重复创建
                .sandboxMap(new RedisSandboxMap(redisTemplate))
                .build();
        return new SandboxService(config);
    }
}