package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.common.model.vo.UserInfoVO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiModelMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiModelDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateModelDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ModelPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateModelDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatApi;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ModelVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectModelVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatSettingService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiModelService;
import cn.iocoder.yudao.module.ai.core.chat.utils.AESEncryptionUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiModelServiceImpl implements AiModelService {

    private final AiModelMapper aiModelMapper;

    private final AiChatSettingService aiChatSettingService;

    private final AclDecisionEngine aclDecisionEngine;

    private final AdminUserService adminUserService;

    private final FileService fileService;

    private final DeptService deptService;

    public AiModelServiceImpl(AiModelMapper aiModelMapper, AiChatSettingService aiChatSettingService, AclDecisionEngine aclDecisionEngine, AdminUserService adminUserService, FileService fileService, DeptService deptService) {
        this.aiModelMapper = aiModelMapper;
        this.aiChatSettingService = aiChatSettingService;
        this.aclDecisionEngine = aclDecisionEngine;
        this.adminUserService = adminUserService;
        this.fileService = fileService;
        this.deptService = deptService;
    }

    @Override
    public ChatApi selectChatModelApiById(Long modelId) {
        return TenantUtils.executeIgnore(() -> aiModelMapper.selectChatModelApiById(modelId));
    }

    @Override
    public SelectModelVO selectModelById(Long modelId) {
        return aiModelMapper.selectModelById(modelId);
    }

    @Override
    public PageResult<ModelVO> pageQuery(ModelPageQueryDTO modelPageQueryDTO) {
        Page<ModelVO> modelVOPage = TenantUtils.executeIgnore(() ->
                aiModelMapper.pageQuery(new Page<>(modelPageQueryDTO.getPage(),
                        modelPageQueryDTO.getPageSize()), modelPageQueryDTO));
        long total = modelVOPage.getTotal();
        List<ModelVO> results = modelVOPage.getRecords();
        results.forEach(modelVO -> {
            Long modelId = modelVO.getId();
            List<Long> userIds = aiChatSettingService.selectUserIdByModelId(modelId);
            List<AdminUserDO> adminBases = adminUserService.getUserByIds(userIds);
            Map<Long, DeptDO> deptMap = deptService.getDeptMap(
                    adminBases.stream().map(AdminUserDO::getDeptId).toList());
            List<Long> avatarIds = adminBases.stream()
                    .map(AdminUserDO::getAvatar)
                    .filter(Objects::nonNull)
                    .map(Long::valueOf).toList();
            List<FileDO> objects = fileService.getFileByIds(avatarIds);
            HashMap<Long, String> hashMap = new HashMap<>();
            objects.forEach(object -> {
                String previewUrl = fileService.presignGetUrl(object.getPath(), 600);
                hashMap.put(object.getId(), previewUrl);
            });
            List<UserInfoVO> list = adminBases.stream().map(adminUserDO -> {
                DeptDO dept = deptMap.get(adminUserDO.getDeptId());
                return UserInfoVO.builder().id(adminUserDO.getId())
                    .email(adminUserDO.getEmail()).introduction(adminUserDO.getRemark())
                            .phone(adminUserDO.getMobile())
                            .department(dept != null ? dept.getName() : null)
                            .username(adminUserDO.getNickname())
                    .imageUrl(adminUserDO.getAvatar() != null
                            ? hashMap.get(Long.valueOf(adminUserDO.getAvatar())) : null)
                    .name(adminUserDO.getNickname()).build();
            }).toList();
            modelVO.setUsers(list);
            // 设置当前用户对该模型的 ACL 权限字符列表
            modelVO.setPermissions(new ArrayList<>(
                    aclDecisionEngine.userPermissions(ResourceType.MODEL, modelVO.getId())));
            // 标记是否为授权过来的内容
            modelVO.setGranted(!aclDecisionEngine.isCreator(ResourceType.MODEL, modelVO.getId()));
        });
        return new PageResult<>(results, total);
    }

    @Override
    public List<SelectModelVO> selectModelList(Boolean filterEmbedding) {
        List<SelectModelVO> all = TenantUtils.executeIgnore(() -> aiModelMapper.selectModelList(filterEmbedding));
        if (all.isEmpty()) {
            return all;
        }
        List<Long> allIds = all.stream().map(SelectModelVO::getId).toList();
        Set<Long> accessibleIds = aclDecisionEngine.accessibleIds(ResourceType.MODEL, Permission.READ, allIds);
        return all.stream().filter(m -> accessibleIds.contains(m.getId())).toList();
    }

    @Override
    public boolean deleteModelById(List<Long> ids) {
        return aiModelMapper.deleteModelById(ids);
    }

    @Override
    public boolean updateModel(UpdateModelDTO model) {
        AiModelDO build = AiModelDO.builder().id(model.getId()).rename(model.getRename()).description(model.getDescription())
                .token(model.getToken()).modelType(model.getModelType()).type(model.getType())
                .name(model.getName()).url(model.getUrl()).key(model.getKey())
                .build();
        String key = model.getKey();
        if (StringUtils.isNoneBlank(key)) {
            build.setKey(AESEncryptionUtil.encrypt(key));
        }
        return aiModelMapper.updateById(build) > 0;
    }

    @Override
    public boolean insertModel(CreateModelDTO model) {
        AiModelDO build = AiModelDO.builder().rename(model.getRename()).description(model.getDescription())
                .token(model.getToken()).modelType(model.getModelType()).type(model.getType())
                .name(model.getName()).url(model.getUrl()).key(model.getKey())
                .isOnline(true).deleted(false).detectionTime(LocalDateTime.now()).build();
        String key = model.getKey();
        if (StringUtils.isNoneBlank(key)) {
            build.setKey(AESEncryptionUtil.encrypt(key));
        }
        return aiModelMapper.insert(build) > 0;
    }

}
