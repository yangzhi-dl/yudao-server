package cn.iocoder.yudao.module.ai.core.tools.skill;

import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiSkillMapper;
import com.alibaba.cloud.ai.graph.skills.SkillMetadata;
import com.alibaba.cloud.ai.graph.skills.registry.AbstractSkillRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.*;

/**
 * 基于数据库的技能注册中心
 * <p>
 * 根据指定的技能 ID 列表从数据库加载技能，仅加载当前智能体配置的技能。
 * 每个智能体使用独立的 DatabaseSkillRegistry 实例，确保技能隔离。
 * <p>
 * 使用方式：
 * <pre>{@code
 * SkillRegistry registry = new DatabaseSkillRegistry(skillMapper, Arrays.asList(1L, 2L, 3L));
 * SkillsAgentHook hook = SkillsAgentHook.builder()
 *     .skillRegistry(registry)
 *     .build();
 * }</pre>
 */
@Slf4j
public class DatabaseSkillRegistry extends AbstractSkillRegistry {

    private static final String DEFAULT_SYSTEM_PROMPT_TEMPLATE = """
           \s
            ## Skills System
           \s
            You have access to a skills library that provides specialized capabilities and domain knowledge. All skills are stored in a database-backed Skill Registry.
           \s
            ### Available Skills
           \s
            {skills_list}
           \s
            ### How to Use Skills (Progressive Disclosure)
           \s
            Skills follow a **progressive disclosure** pattern - you know they exist (name + description above), but you only read the full instructions when needed:
           \s
            1. **Recognize when a skill applies**: Check if the user's task matches any skill's description
            2. **Read the skill's full instructions**: Use `read_skill` with the skill name shown above
            3. **Follow the skill's instructions**: SKILL.md contains step-by-step workflows, best practices, and examples
            4. **Execute skill scripts**: Skills may include Python/Shell scripts that can be executed
           \s
            #### How to Read The Full Skill Instruction
           \s
            You are currently using a database-backed Skill Registry. Please follow the skill loading guidelines below:
           \s
            {skills_load_instructions}
           \s
            **Important:**
           \s
              - **For reading skill instructions**: Always use `read_skill` to read skill instructions.\s
              - **For executing skill scripts**: Use the `execute_skill` tool or relevant sandbox tools to run skill scripts.
           \s
            #### When to Use Skills
           \s
              - When the user's request matches a skill's domain\s
              - When you need specialized knowledge or structured workflows
              - When a skill provides proven patterns for complex tasks
           \s
            ### Example Workflow
           \s
            User: "Can you research the latest developments in quantum computing?"
           \s
            1. Check available skills above → See relevant skill with its name
            2. Read the skill using `read_skill` with the skill name
            3. Follow the skill's workflow
            4. Execute any scripts as needed
           \s
            Remember: Skills are tools to make you more capable and consistent. When in doubt, check if a skill exists for the task!
           \s""";

    private final AiSkillMapper skillMapper;
    private final List<Long> skillIds;
    private final SystemPromptTemplate systemPromptTemplate;

    /**
     * 创建数据库技能注册中心
     *
     * @param skillMapper 技能 Mapper
     * @param skillIds    要加载的技能 ID 列表（仅加载这些技能）
     */
    public DatabaseSkillRegistry(AiSkillMapper skillMapper, List<Long> skillIds) {
        if (skillMapper == null) {
            throw new IllegalArgumentException("SkillMapper must not be null");
        }
        this.skillMapper = skillMapper;
        this.skillIds = skillIds != null ? new ArrayList<>(skillIds) : Collections.emptyList();
        this.systemPromptTemplate = SystemPromptTemplate.builder()
                .template(DEFAULT_SYSTEM_PROMPT_TEMPLATE)
                .build();
        loadSkillsToRegistry();
    }

    /**
     * 创建数据库技能注册中心，使用自定义系统提示模板
     */
    public DatabaseSkillRegistry(AiSkillMapper skillMapper, List<Long> skillIds, String systemPromptTemplate) {
        if (skillMapper == null) {
            throw new IllegalArgumentException("SkillMapper must not be null");
        }
        this.skillMapper = skillMapper;
        this.skillIds = skillIds != null ? new ArrayList<>(skillIds) : Collections.emptyList();
        this.systemPromptTemplate = SystemPromptTemplate.builder()
                .template(systemPromptTemplate != null ? systemPromptTemplate : DEFAULT_SYSTEM_PROMPT_TEMPLATE)
                .build();
        loadSkillsToRegistry();
    }

    @Override
    @DataPermission(enable = false)
    protected void loadSkillsToRegistry() {
        Map<String, SkillMetadata> loadedSkills = new HashMap<>();

        if (skillIds.isEmpty()) {
            log.debug("没有配置技能 ID，跳过加载");
            this.skills = loadedSkills;
            return;
        }

        try {
            List<AiSkillDO> skillDOs = skillMapper.selectByIds(skillIds);
            log.info("从数据库加载了 {}/{} 个技能", skillDOs.size(), skillIds.size());

            for (AiSkillDO skillDO : skillDOs) {
                // 跳过禁用的技能
                if (skillDO.getStatus() != null && skillDO.getStatus() == 0) {
                    log.debug("技能已禁用，跳过: {}", skillDO.getName());
                    continue;
                }

                SkillMetadata metadata = SkillMetadata.builder()
                        .name(skillDO.getName())
                        .description(skillDO.getDescription() != null ? skillDO.getDescription() : "")
                        .skillPath("db://skills/" + skillDO.getName())
                        .source("database")
                        .fullContent(skillDO.getSkillMdContent() != null ? skillDO.getSkillMdContent() : "")
                        .build();

                loadedSkills.put(metadata.getName(), metadata);
            }
        } catch (Exception e) {
            log.error("加载技能失败", e);
        }

        this.skills = loadedSkills;
        log.info("技能注册完成: {} 个技能", loadedSkills.size());
    }

    @Override
    public String readSkillContent(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be null or empty");
        }

        Optional<SkillMetadata> skillOpt = get(name);
        if (skillOpt.isEmpty()) {
            throw new IllegalStateException("Skill not found: " + name);
        }

        SkillMetadata skill = skillOpt.get();
        String fullContent = skill.getFullContent();

        if (fullContent == null || fullContent.isEmpty()) {
            return "该技能暂无 SKILL.md 文档内容。\n技能名称: " + skill.getName() + "\n技能描述: " + skill.getDescription();
        }

        return fullContent;
    }

    @Override
    public String getSkillLoadInstructions() {
        return """
                **Skill Registry Type:** Database-backed Skill Registry
                
                **Skill Path Format:**
                Each skill has a unique name shown in the skill list above. \
                Use the exact skill name when calling `read_skill` to read the SKILL.md content.
                
                **Available Operations:**
                - Use `read_skill` with the skill name to read the full skill instructions
                - Skill scripts can be executed using available sandbox tools
                """;
    }

    @Override
    public String getRegistryType() {
        return "Database";
    }

    @Override
    public SystemPromptTemplate getSystemPromptTemplate() {
        return systemPromptTemplate;
    }

    /**
     * 获取当前加载的技能 ID 列表
     */
    public List<Long> getSkillIds() {
        return Collections.unmodifiableList(skillIds);
    }
}