package cn.iocoder.yudao.module.ai.core.tools.skill;

import io.agentscope.runtime.sandbox.manager.client.container.BaseClient;
import io.agentscope.runtime.sandbox.manager.client.container.BaseClientStarter;
import io.agentscope.runtime.sandbox.manager.client.container.docker.DockerClientStarter;
import io.agentscope.runtime.sandbox.manager.model.container.ContainerClientType;
import io.agentscope.runtime.sandbox.manager.utils.PortManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 跨主机 Docker 客户端启动器。
 * <p>
 * 委托给原生 {@link DockerClientStarter}，但创建的是 {@link RemoteDockerClient}，
 * 使沙箱容器连接地址使用远端 Docker 主机 IP 而非 localhost。
 */
@Slf4j
public class RemoteDockerClientStarter extends BaseClientStarter {

    private final DockerClientStarter delegate;
    private final String remoteHost;

    public RemoteDockerClientStarter(DockerClientStarter delegate, String remoteHost) {
        super(ContainerClientType.DOCKER);
        this.delegate = delegate;
        this.remoteHost = remoteHost;
    }

    @Override
    public BaseClient startClient(PortManager portManager) {
        RemoteDockerClient client = new RemoteDockerClient(delegate, portManager, remoteHost);
        client.connect();
        log.info("Docker client created (remote host: {})", remoteHost);
        return client;
    }
}