package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiModelDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelSettingDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatSettingsMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiModelMapper;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SaveModelSettingDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AssignModelResultVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatSettingService;
import cn.iocoder.yudao.module.system.api.message.user.AdminUserCreatedOrUpdatedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.MODEL_NOT_EXISTS;

@Service
@Slf4j
public class AiChatSettingServiceImpl implements AiChatSettingService {

    private final AiChatSettingsMapper settingsMapper;
    private final AiModelMapper modelMapper;

    public AiChatSettingServiceImpl(AiChatSettingsMapper settingsMapper, AiModelMapper modelMapper) {
        this.settingsMapper = settingsMapper;
        this.modelMapper = modelMapper;
    }

    @EventListener
    public void initUserModelSetting(AdminUserCreatedOrUpdatedMessage message) {
        initUserModelSetting(message.getUserId());
    }

    @Override
    public void initUserModelSetting(Long userId) {
        if (settingsMapper.getModelSettingByUserId(userId) != null) {
            return;
        }
        List<ModelConfig> configs = new ArrayList<>();
        List<AiModelDO> models = modelMapper.selectList(new LambdaQueryWrapperX<AiModelDO>()
                .eq(AiModelDO::getDeleted, false));
        Arrays.stream(AiModelType.values()).forEach(type -> models.stream()
                .filter(model -> model.getModelType() == type)
                .findFirst()
                .ifPresent(model -> configs.add(ModelConfig.builder().id(model.getId())
                        .type(type).build())));
        ModelSettingDO setting = ModelSettingDO.builder().userId(userId).configs(configs).build();
        setting.setCreator(String.valueOf(userId));
        settingsMapper.insert(setting);
    }

    @Override
    public void saveUserModelSetting(Long userId, List<ModelConfig> configs) {
        ModelSettingDO existing = settingsMapper.getModelSettingByUserId(userId);
        if (existing == null) {
            ModelSettingDO setting = ModelSettingDO.builder().userId(userId).configs(configs).build();
            setting.setCreator(String.valueOf(userId));
            settingsMapper.insert(setting);
        } else {
            settingsMapper.updateModelSettingByUser(
                    new SaveModelSettingDTO(null, String.valueOf(userId), configs));
        }
    }

    @Override
    public ModelSettingDO getUserModelSetting(String creator) {
        return settingsMapper.getModelSettingByUserId(creator);
    }

    @Override
    public List<Long> selectUserIdByModelId(Long modelId) {
        return settingsMapper.selectUserIdByModelId(modelId);
    }

    @Override
    public List<Long> selectConfiguredUserIds(java.util.Collection<Long> userIds) {
        return settingsMapper.selectConfiguredUserIds(userIds);
    }

    @Override
    public void delete(String creator) {
        settingsMapper.deleteByCreator(creator);
    }

    @Override
    public Boolean saveUserModelSetting(SaveModelSettingDTO dto) {
        String creator = dto.getCreator();
        ModelSettingDO existing = settingsMapper.getModelSettingByUserId(creator);
        if (existing == null) {
            Long loginUserId = getLoginUserId();
            ModelSettingDO setting = ModelSettingDO.builder()
                    .userId(loginUserId)
                    .configs(dto.getConfigs())
                    .build();
            setting.setCreator(String.valueOf(loginUserId));
            settingsMapper.insert(setting);
            return true;
        } else {
            return settingsMapper.updateModelSettingByUser(dto) > 0;
        }
    }

    @Override
    public AssignModelResultVO assignModelToUser(Long userId, Long modelId, Boolean force) {
        AiModelDO model = modelMapper.selectById(modelId);
        if (model == null) {
            throw exception(MODEL_NOT_EXISTS);
        }
        AiModelType modelType = model.getModelType();
        ModelSettingDO setting = settingsMapper.getModelSettingByUserId(userId);
        List<ModelConfig> configs = setting != null && setting.getConfigs() != null
                ? new ArrayList<>(setting.getConfigs()) : new ArrayList<>();
        ModelConfig existing = configs.stream()
                .filter(c -> c.getType() == modelType)
                .findFirst().orElse(null);
        // 该用户该类型槽位已有配置
        if (existing != null) {
            if (existing.getId().equals(modelId)) {
                // 已设置为当前模型
                return AssignModelResultVO.builder()
                        .changed(AssignModelResultVO.CHANGED_NOOP).build();
            }
            if (!Boolean.TRUE.equals(force)) {
                // 冲突：该类型已有其他默认模型
                return AssignModelResultVO.builder()
                        .changed(AssignModelResultVO.CHANGED_CONFLICT)
                        .currentModelId(existing.getId())
                        .build();
            }
            // 强制覆盖该类型槽位
            existing.setId(modelId);
        } else {
            configs.add(ModelConfig.builder().id(modelId).type(modelType).build());
        }
        persistConfigs(userId, setting, configs);
        return AssignModelResultVO.builder()
                .changed(existing == null ? AssignModelResultVO.CHANGED_ADDED
                        : AssignModelResultVO.CHANGED_REPLACED)
                .build();
    }

    /**
     * 写入用户模型设置：无记录则新增，有记录则按 creator 更新（configs 完整覆盖）。
     */
    private void persistConfigs(Long userId, ModelSettingDO setting, List<ModelConfig> configs) {
        if (setting == null) {
            ModelSettingDO newSetting = ModelSettingDO.builder().userId(userId).configs(configs).build();
            newSetting.setCreator(String.valueOf(userId));
            settingsMapper.insert(newSetting);
        } else {
            settingsMapper.updateModelSettingByUser(
                    new SaveModelSettingDTO(null, String.valueOf(userId), configs));
        }
    }
}
