package cn.iocoder.yudao.module.ai.core.tools.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.runtime.sandbox.manager.model.container.ContainerModel;
import io.agentscope.runtime.sandbox.manager.model.container.SandboxKey;
import io.agentscope.runtime.sandbox.manager.utils.SandboxMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 基于 Redis 的沙箱容器注册表，替换库默认的 {@code InMemorySandboxMap}。
 * <p>
 * 背景：默认注册表仅进程内可见，服务重启（尤其强杀后旧容器残留）或新开一个应用实例时，
 * 无法感知已存在的容器，会为同一用户重复创建沙箱容器。
 * <p>
 * 本实现把 {@code (userId, sessionId, sandboxType) -> ContainerModel} 的映射持久化到 Redis，
 * 重启/多实例共用同一注册表，库的 {@code createContainer} 去重逻辑命中后直接复用已有容器，
 * 从根源上杜绝重复创建。
 * <p>
 * 每个实例在创建容器时记录 owner（当前实例 ID）。{@link #getAllSandboxes()} 只返回本实例
 * 创建的容器，这样某实例优雅关闭（cleanupAllSandboxes）时只清理自己创建的容器，不会误删
 * 其它实例正在复用的共享容器。
 */
@Slf4j
public class RedisSandboxMap implements SandboxMap {

    private static final String MAP_PREFIX = "iims:sandbox:map:";
    private static final String OWNER_PREFIX = "iims:sandbox:owner:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 当前应用实例 ID，用于标记容器归属，避免某实例关闭时误删其它实例共享的容器
     */
    private final String ownerId = UUID.randomUUID().toString();

    public RedisSandboxMap(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String mapKey(SandboxKey key) {
        return MAP_PREFIX + key.userID() + ":" + key.sessionID() + ":" + key.sandboxType();
    }

    private String ownerKey(SandboxKey key) {
        return OWNER_PREFIX + key.userID() + ":" + key.sessionID() + ":" + key.sandboxType();
    }

    @Override
    public void addSandbox(SandboxKey sandboxKey, ContainerModel containerModel) {
        if (sandboxKey == null || containerModel == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(mapKey(sandboxKey), objectMapper.writeValueAsString(containerModel));
            redisTemplate.opsForValue().set(ownerKey(sandboxKey), ownerId);
        } catch (Exception e) {
            log.error("保存沙箱容器映射失败: {}", sandboxKey, e);
        }
    }

    @Override
    public ContainerModel getSandbox(SandboxKey sandboxKey) {
        if (sandboxKey == null) {
            return null;
        }
        return readContainer(mapKey(sandboxKey));
    }

    @Override
    public boolean removeSandbox(SandboxKey sandboxKey) {
        if (sandboxKey == null) {
            return false;
        }
        try {
            Boolean deleted = redisTemplate.delete(mapKey(sandboxKey));
            redisTemplate.delete(ownerKey(sandboxKey));
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("删除沙箱容器映射失败: {}", sandboxKey, e);
            return false;
        }
    }

    @Override
    public ContainerModel getSandbox(String containerId) {
        if (!StringUtils.hasText(containerId)) {
            return null;
        }
        for (String key : keys(MAP_PREFIX)) {
            ContainerModel model = readContainer(key);
            if (model != null && containerId.equals(model.getContainerId())) {
                return model;
            }
        }
        return null;
    }

    @Override
    public void removeSandbox(String containerId) {
        if (!StringUtils.hasText(containerId)) {
            return;
        }
        for (String key : keys(MAP_PREFIX)) {
            ContainerModel model = readContainer(key);
            if (model != null && containerId.equals(model.getContainerId())) {
                redisTemplate.delete(key);
                redisTemplate.delete(key.replace(MAP_PREFIX, OWNER_PREFIX));
                return;
            }
        }
    }

    @Override
    public Map<String, ContainerModel> getAllSandboxes() {
        Map<String, ContainerModel> result = new HashMap<>();
        for (String key : keys(MAP_PREFIX)) {
            // 只返回本实例创建的容器，避免关闭时误删其它实例正在复用的共享容器
            String owner = redisTemplate.opsForValue().get(key.replace(MAP_PREFIX, OWNER_PREFIX));
            if (!ownerId.equals(owner)) {
                continue;
            }
            ContainerModel model = readContainer(key);
            if (model != null) {
                result.put(model.getContainerId(), model);
            }
        }
        return result;
    }

    @Override
    public boolean containSandbox(SandboxKey sandboxKey) {
        if (sandboxKey == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(mapKey(sandboxKey)));
    }

    @Override
    public boolean containSandbox(String containerId) {
        return getSandbox(containerId) != null;
    }

    private Set<String> keys(String prefix) {
        return redisTemplate.keys(prefix + "*");
    }

    private ContainerModel readContainer(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            return objectMapper.readValue(json, ContainerModel.class);
        } catch (Exception e) {
            log.error("读取沙箱容器映射失败: {}", key, e);
            return null;
        }
    }
}
