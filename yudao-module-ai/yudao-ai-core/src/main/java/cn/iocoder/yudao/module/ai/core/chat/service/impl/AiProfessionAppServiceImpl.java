package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.module.ai.core.chat.enums.AgentStatus;
import cn.iocoder.yudao.module.ai.core.chat.service.AiProfessionAppService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class AiProfessionAppServiceImpl implements AiProfessionAppService {

    private final StringRedisTemplate redisTemplate;

    public AiProfessionAppServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 更新单个智能体状态
    @Override
    public void updateStatus(Long agentId, AgentStatus status) {
        String value = String.valueOf(agentId);
        String statusKey = "iims:statistics:aiAgent:status";   // agentId → statusCode
        String countKey = "iims:statistics:aiAgent:count";     // statusCode → count
        String detailKey = "iims:statistics:aiAgent:detail:" + value;
        String historyKey = "iims:statistics:aiAgent:history:" + value;
        long now = System.currentTimeMillis();

        // Lua 脚本：原子性更新状态 + 计数
        String luaScript =
                """
                    local statusKey = KEYS[1]
                    local countKey = KEYS[2]
                    local agentId = ARGV[1]
                    local newStatus = ARGV[2]
                    
                    -- 获取旧状态
                    local oldStatus = redis.call('hget', statusKey, agentId)
                    
                    -- 旧状态计数 -1
                    if oldStatus then
                        redis.call('hincrby', countKey, oldStatus, -1)
                    end
                    
                    -- 更新 aiAgent 状态
                    redis.call('hset', statusKey, agentId, newStatus)
                    
                    -- 新状态计数 +1
                    redis.call('hincrby', countKey, newStatus, 1)
                    
                    return 1""";

        // 执行 Lua 脚本（只需执行一次！）
        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Arrays.asList(statusKey, countKey),  // 两个 key
                value, status.code                   // agentId, newStatusCode
        );

        // 存储详情 (Hash) - 非原子操作，可独立执行
        Map<String, String> detail = new HashMap<>();
        detail.put("id", value);
        detail.put("status", status.code);
        detail.put("lastUpdate", String.valueOf(now));
        redisTemplate.opsForHash().putAll(detailKey, detail);

        // 记录历史 (Sorted Set)
        redisTemplate.opsForZSet().add(historyKey, status.code + ":" + now, now);
        redisTemplate.expire(historyKey, Duration.ofDays(7));

        // 刷新心跳 (String)
        redisTemplate.opsForValue()
                .set("iims:statistics:aiAgent:heartbeat:" + value, String.valueOf(now), Duration.ofSeconds(30));
    }

    // 获取全局统计
    @Override
    public Map<String, Integer> getGlobalStatistics() {
        // 直接从计数 Hash 读取，无需遍历聚合
        Map<Object, Object> countEntries = redisTemplate.opsForHash()
                .entries("iims:statistics:aiAgent:count");

        // 初始化默认值
        Map<String, Integer> statistics = new HashMap<>(Map.of(
                "running", 0,
                "idle", 0,
                "maintenance", 0,
                "error", 0
        ));

        // 填充实际计数 - 处理类型转换
        for (Map.Entry<Object, Object> entry : countEntries.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            Integer intValue = Integer.parseInt(value.toString());
            statistics.put(key, intValue);
        }
        return statistics;
    }

}
