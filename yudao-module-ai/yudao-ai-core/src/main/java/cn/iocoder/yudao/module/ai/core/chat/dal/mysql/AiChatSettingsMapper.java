package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelSettingDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SaveModelSettingDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI 模型设置 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiChatSettingsMapper extends BaseMapperX<ModelSettingDO> {

    /**
     * 按用户 ID 查询模型设置
     */
    default ModelSettingDO getModelSettingByUserId(String creator) {
        return selectOne(new LambdaQueryWrapperX<ModelSettingDO>()
                .eq(ModelSettingDO::getCreator, creator).last("limit 1"));
    }

    /**
     * 按用户主键查询模型设置。
     */
    default ModelSettingDO getModelSettingByUserId(Long userId) {
        return selectById(userId);
    }

    /**
     * 查询使用指定模型的所有用户 ID（JSON 数组内匹配）
     */
    List<Long> selectUserIdByModelId(Long modelId);

    /** 查询已有模型设置的用户编号 */
    default List<Long> selectConfiguredUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<ModelSettingDO>()
                .select(ModelSettingDO::getUserId)
                .in(ModelSettingDO::getUserId, userIds)
                .eq(ModelSettingDO::getDeleted, false))
                .stream().map(ModelSettingDO::getUserId).toList();
    }

    /**
     * 按用户 ID 删除模型设置
     */
    default void deleteByCreator(String creator) {
        delete(new LambdaQueryWrapperX<ModelSettingDO>()
                .eq(ModelSettingDO::getCreator, creator));
    }

    /**
     * 按用户更新模型设置（configs 完整覆盖，由 Service 层合并）
     */
    int updateModelSettingByUser(SaveModelSettingDTO dto);

}
