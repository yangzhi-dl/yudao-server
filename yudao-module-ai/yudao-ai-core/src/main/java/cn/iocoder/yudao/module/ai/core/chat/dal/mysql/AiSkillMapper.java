package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SkillPageQueryDTO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * AI 技能 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiSkillMapper extends BaseMapperX<AiSkillDO> {

    /**
     * 分页查询
     */
    default PageResult<AiSkillDO> pageQuery(SkillPageQueryDTO dto) {
        LambdaQueryWrapperX<AiSkillDO> wrapper = new LambdaQueryWrapperX<AiSkillDO>()
                .likeIfPresent(AiSkillDO::getName, dto.getName())
                .eqIfPresent(AiSkillDO::getStatus, dto.getStatus())
                .orderByDesc(AiSkillDO::getId);
        if (!CollectionUtils.isEmpty(dto.getFilterIds())) {
            wrapper.in(AiSkillDO::getId, dto.getFilterIds());
        }
        return selectPage(dto, wrapper);
    }

    /**
     * 按 ID 查询技能
     */
    default AiSkillDO selectById(Long id) {
        return selectOne(new LambdaQueryWrapperX<AiSkillDO>()
                .eq(AiSkillDO::getId, id)
                .eq(AiSkillDO::getDeleted, false));
    }

    /**
     * 软删除技能
     */
    default Boolean delete(List<Long> ids) {
        update(new LambdaUpdateWrapper<AiSkillDO>()
                .in(AiSkillDO::getId, ids)
                .set(AiSkillDO::getDeleted, true));
        return true;
    }

    /**
     * 批量按 ID 查询技能（仅返回未删除且启用的）
     */
    default List<AiSkillDO> selectByIds(List<Long> ids) {
        return selectList(new LambdaQueryWrapperX<AiSkillDO>()
                .in(AiSkillDO::getId, ids)
                .eq(AiSkillDO::getDeleted, false));
    }

    /**
     * 按名称查询技能（仅返回未删除且启用的）
     */
    default AiSkillDO selectByName(String name) {
        return selectOne(new LambdaQueryWrapperX<AiSkillDO>()
                .eq(AiSkillDO::getName, name)
                .eq(AiSkillDO::getDeleted, false));
    }

}