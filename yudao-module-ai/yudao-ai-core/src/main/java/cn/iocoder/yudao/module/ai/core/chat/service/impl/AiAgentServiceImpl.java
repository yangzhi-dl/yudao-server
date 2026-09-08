package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiAgentMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.AgentPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateAgentDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateAgentDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AgentDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AgentVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectAgentVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectModelVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiAgentService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiAgentServiceImpl implements AiAgentService {

    private final AiAgentMapper aiAgentMapper;

    private final AiModelService aiModelService;

    private final AclDecisionEngine aclDecisionEngine;

    private final FileApi fileApi;

    private final DictDataService dictDataService;

    private final static Integer EXPIRATION_SECONDS = 3000;

    public AiAgentServiceImpl(AiAgentMapper aiAgentMapper, AiModelService aiModelService,
                              AclDecisionEngine aclDecisionEngine, FileApi fileApi, DictDataService dictDataService) {
        this.aiAgentMapper = aiAgentMapper;
        this.aiModelService = aiModelService;
        this.aclDecisionEngine = aclDecisionEngine;
        this.fileApi = fileApi;
        this.dictDataService = dictDataService;
    }

    @Override
    public List<SelectAgentVO> selectAgentList() {
        List<SelectAgentVO> all = TenantUtils.executeIgnore(aiAgentMapper::selectAgentList);
        if (all.isEmpty()) {
            return all;
        }
        List<Long> allIds = all.stream().map(SelectAgentVO::getId).toList();
        Set<Long> accessibleIds = aclDecisionEngine.accessibleIds(ResourceType.AGENT, Permission.READ, allIds);
        return all.stream().filter(a -> accessibleIds.contains(a.getId())).toList();
    }

    @Override
    public AiAgentDO selectAccessibleAgentById(Long id) {
        return TenantUtils.executeIgnore(() -> aiAgentMapper.selectAgentById(id));
    }

    @Override
    public Boolean increaseUsageCount(Long id) {
        return aiAgentMapper.increaseUsageCount(id);
    }

    @Override
    public AgentDetailVO selectAgentById(Long id) {
        AiAgentDO aiAgentDO = aiAgentMapper.selectAgentById(id);
        Long cover = aiAgentDO.getCover();
        AgentDetailVO build = AgentDetailVO.builder().tools(aiAgentDO.getTools())
                .categoryId(aiAgentDO.getCategoryId()).settings(aiAgentDO.getSettings())
                .skillIds(aiAgentDO.getSkillIds())
                .tagIds(aiAgentDO.getTagIds())
                .imageUrl(Objects.nonNull(cover)
                        ? fileApi.presignGetUrl(cover, EXPIRATION_SECONDS) : null).build();
        BeanUtils.copyProperties(aiAgentDO, build);
        return build;
    }

    @Override
    public PageResult<AgentVO> pageQuery(AgentPageQueryDTO dto) {
        // 跨租户分页查询智能体，ACL 数据权限规则会自动注入 id NOT IN (当前用户无权访问的智能体 ID)
        Page<AiAgentDO> agentPage = TenantUtils.executeIgnore(() -> aiAgentMapper.pageQuery(new Page<>(dto.getPage(), dto.getPageSize()), dto));
        List<AiAgentDO> result = agentPage.getRecords();
        List<AgentVO> list = new ArrayList<>();
        result.forEach(agent -> {
            AgentVO build = AgentVO.builder().build();
            BeanUtils.copyProperties(agent, build);
            if (agent.getModelId() != null) {
                SelectModelVO model = aiModelService.selectModelById(agent.getModelId());
                if (model != null) {
                    build.setModelType(model.getModelType());
                    build.setModelName(Objects.isNull(model.getRename()) ? agent.getName() : model.getRename());
                }
            }
            if (agent.getCover() != null) {
                build.setImageUrl(fileApi.presignGetUrl(agent.getCover(), EXPIRATION_SECONDS));
            }
            DictDataDO dictDataDO = dictDataService.getDictData(agent.getCategoryId());
            if (Objects.nonNull(dictDataDO)) {
                build.setCategoryName(dictDataDO.getLabel());
            }
            List<Long> tagIds = agent.getTagIds();
            if (Objects.nonNull(tagIds)) {
                List<String> tagsName = new ArrayList<>();
                List<DictDataDO> dictDataByIds = dictDataService.getDictDataByIds(tagIds);
                dictDataByIds.forEach(dictTagId -> {
                    tagsName.add(dictTagId.getValue());
                });
                build.setTagNames(tagsName);
            }
            // 设置当前用户对该智能体的 ACL 权限字符列表
            build.setPermissions(new ArrayList<>(
                    aclDecisionEngine.userPermissions(ResourceType.AGENT, agent.getId())));
            // 标记是否为授权过来的内容
            build.setGranted(!aclDecisionEngine.isCreator(ResourceType.AGENT, agent.getId()));
            list.add(build);
        });
        return new PageResult<>(list, agentPage.getTotal());
    }

    @Override
    public Boolean updateData(UpdateAgentDTO dto) {
        return aiAgentMapper.updateAgent(AiAgentDO.builder()
                .id(dto.getId()).modelId(dto.getModelId()).tagIds(dto.getTagIds())
                .categoryId(dto.getCategoryId()).tools(dto.getTools()).prompt(dto.getPrompt())
                .name(dto.getName()).description(dto.getDescription()).cover(dto.getCover())
                .settings(dto.getSettings()).skillIds(dto.getSkillIds()).build()) > 0;
    }

    @Override
    public Boolean delete(List<Long> ids) {
        return aiAgentMapper.delete(ids);
    }

    @Override
    public Boolean insert(CreateAgentDTO dto) {
        AiAgentDO build = AiAgentDO.builder()
                .modelId(dto.getModelId()).tagIds(dto.getTagIds()).categoryId(dto.getCategoryId())
                .tools(dto.getTools()).prompt(dto.getPrompt()).type(dto.getType())
                .name(dto.getName()).description(dto.getDescription()).cover(dto.getCover())
                .settings(dto.getSettings()).skillIds(dto.getSkillIds()).build();
        return aiAgentMapper.insertAgent(build) > 0;
    }
}
