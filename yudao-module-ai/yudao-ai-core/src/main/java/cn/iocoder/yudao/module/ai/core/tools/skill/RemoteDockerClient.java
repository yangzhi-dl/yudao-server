package cn.iocoder.yudao.module.ai.core.tools.skill;

import io.agentscope.runtime.sandbox.manager.client.container.ContainerCreateResult;
import io.agentscope.runtime.sandbox.manager.client.container.docker.DockerClient;
import io.agentscope.runtime.sandbox.manager.client.container.docker.DockerClientStarter;
import io.agentscope.runtime.sandbox.manager.model.fs.VolumeBinding;
import io.agentscope.runtime.sandbox.manager.utils.PortManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 跨主机 Docker 沙箱客户端。
 * <p>
 * 库原生的 {@link DockerClient#createContainer} 会把容器连接地址的 IP 硬编码为 {@code localhost}，
 * 仅适用于应用与 Docker 在同一台机器的场景。当应用（如 Linux 容器）与 Docker（如 Windows 主机）不在同一台
 * 机器时，客户端会去连接自身 localhost 上的映射端口而失败。
 * <p>
 * 本类覆写 {@link #createContainer}，在创建成功后把 IP 替换为 Docker 所在主机的可访问地址
 * （{@code remoteHost}），从而让 {@code baseUrl} 指向远端 Docker 的映射端口。
 * 本地开发时 {@code remoteHost} 与应用所在机器一致（如 localhost），行为与原生一致。
 */
@Slf4j
public class RemoteDockerClient extends DockerClient {

    private final String remoteHost;

    public RemoteDockerClient(DockerClientStarter config, PortManager portManager, String remoteHost) {
        super(config, portManager);
        this.remoteHost = remoteHost;
    }

    @Override
    public ContainerCreateResult createContainer(String containerName, String imageName,
                                                 List<String> ports,
                                                 List<VolumeBinding> volumeBindings,
                                                 Map<String, String> environment,
                                                 Map<String, Object> runtimeConfig) {
        ContainerCreateResult result = super.createContainer(containerName, imageName, ports,
                volumeBindings, environment, runtimeConfig);
        if (result != null && StringUtils.hasText(remoteHost)) {
            result.setIp(remoteHost);
            log.info("已设置沙箱容器连接 IP 为远端 Docker 主机: {}", remoteHost);
        }
        return result;
    }
}