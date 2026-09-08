package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelSettingDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SaveModelSettingDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AssignModelResultVO;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

public interface AiChatSettingService {

    ModelSettingDO getUserModelSetting(String creator);

    default ModelSettingDO getUserModelSetting() {
        String loginUserId = String.valueOf(getLoginUserId());
        return getUserModelSetting(loginUserId);
    }

    List<Long> selectUserIdByModelId(Long modelId);

    List<Long> selectConfiguredUserIds(Collection<Long> userIds);

    void delete(String creator);

    Boolean saveUserModelSetting(SaveModelSettingDTO dto);

    /**
     * 按指定用户保存模型设置；用于管理员对目标用户的配置管理。
     *
     * @param userId 目标用户编号
     * @param configs 完整配置列表
     */
    void saveUserModelSetting(Long userId, List<cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelConfig> configs);

    /**
     * 为用户补齐默认模型配置；已有用户配置不覆盖。
     *
     * @param userId 用户编号
     */
    void initUserModelSetting(Long userId);

    /**
     * 模型为中心：将指定模型设置为指定用户其「对应类型」的默认模型。
     *
     * <p>按该模型所属 {@code modelType} 定位用户 configs 中的槽位：无则新增；已是同一模型返回 NOOP；
     * 已有其他模型时，除非 force 否则返回 CONFLICT（需前端确认后重试）。每个类型每个用户仅维护一个默认模型。</p>
     *
     * @param userId  目标用户编号
     * @param modelId 模型编号
     * @param force   是否强制覆盖该类型已有默认模型
     * @return 设置结果 {@link AssignModelResultVO}
     */
    AssignModelResultVO assignModelToUser(Long userId, Long modelId, Boolean force);
}
