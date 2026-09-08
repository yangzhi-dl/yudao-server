package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillResourceDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 技能资源 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiSkillResourceMapper extends BaseMapperX<AiSkillResourceDO> {

    /**
     * 按技能 ID 查询资源列表
     */
    default List<AiSkillResourceDO> selectListBySkillId(Long skillId) {
        return selectList(new LambdaQueryWrapperX<AiSkillResourceDO>()
                .eq(AiSkillResourceDO::getSkillId, skillId)
                .eq(AiSkillResourceDO::getDeleted, false)
                .orderByAsc(AiSkillResourceDO::getSortOrder));
    }

    /**
     * 批量删除资源
     */
    default Boolean delete(List<Long> ids) {
        update(new LambdaUpdateWrapper<AiSkillResourceDO>()
                .in(AiSkillResourceDO::getId, ids)
                .set(AiSkillResourceDO::getDeleted, true));
        return true;
    }

    /**
     * 按技能 ID 删除所有资源
     */
    default Boolean deleteBySkillId(Long skillId) {
        update(new LambdaUpdateWrapper<AiSkillResourceDO>()
                .eq(AiSkillResourceDO::getSkillId, skillId)
                .set(AiSkillResourceDO::getDeleted, true));
        return true;
    }

}