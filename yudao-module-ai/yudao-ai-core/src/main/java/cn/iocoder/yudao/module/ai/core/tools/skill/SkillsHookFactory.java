package cn.iocoder.yudao.module.ai.core.tools.skill;

import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiSkillMapper;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 技能 Hook 工厂
 * <p>
 * 根据智能体配置的技能 ID 列表，创建对应的 SkillsAgentHook 实例。
 * 每个智能体调用时创建独立的 SkillRegistry + SkillsAgentHook，确保技能隔离。
 * <p>
 * 使用方式：
 * <pre>{@code
 * SkillsAgentHook hook = skillsHookFactory.createHook(agent.getSkillIds());
 * ReactAgent agent = ReactAgent.builder()
 *     .name("my-agent")
 *     .model(chatModel)
 *     .hooks(List.of(hook))
 *     .build();
 * }</pre>
 */
@Slf4j
@Component
public class SkillsHookFactory {

    private final AiSkillMapper skillMapper;

    public SkillsHookFactory(AiSkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }

    /**
     * 根据技能 ID 列表创建 SkillsAgentHook
     * <p>
     * 仅加载指定 ID 的技能，不与全局技能列表混用。
     *
     * @param skillIds 技能 ID 列表（为空或 null 时，Hook 不加载任何技能）
     * @return SkillsAgentHook 实例
     */
    public SkillsAgentHook createHook(List<Long> skillIds) {
        List<Long> ids = skillIds != null ? skillIds : Collections.emptyList();

        if (ids.isEmpty()) {
            log.debug("技能 ID 列表为空，创建空技能注册中心");
        } else {
            log.info("创建技能 Hook，技能 ID: {}", ids);
        }

        // 创建独立的数据库技能注册中心
        SkillRegistry registry = new DatabaseSkillRegistry(skillMapper, ids);

        // 构建 SkillsAgentHook
        return SkillsAgentHook.builder()
                .skillRegistry(registry)
                .autoReload(false) // 数据库技能不需要自动重载
                .build();
    }

    /**
     * 创建空的 SkillsAgentHook（不加载任何技能）
     */
    public SkillsAgentHook createEmptyHook() {
        return createHook(Collections.emptyList());
    }

    /**
     * 判断技能 ID 列表是否有效（非空且有内容）
     */
    public boolean hasSkills(List<Long> skillIds) {
        return skillIds != null && !skillIds.isEmpty();
    }
}