package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiModelDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ModelPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatApi;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ModelVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectModelVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 模型 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiModelMapper extends BaseMapperX<AiModelDO> {

    /**
     * 查询 ChatApi 信息（XML）
     */
    ChatApi selectChatModelApiById(Long id);

    /**
     * 分页查询（XML - 动态 WHERE 条件复杂）
     */
    Page<ModelVO> pageQuery(@Param("page") Page<ModelVO> page, @Param("dto") ModelPageQueryDTO modelPageQueryDTO);

    /**
     * 按 ID 查询模型
     */
    default SelectModelVO selectModelById(Long id) {
        AiModelDO entity = selectById(id);
        if (entity == null) {
            return null;
        }
        SelectModelVO vo = new SelectModelVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setRename(entity.getRename());
        vo.setType(entity.getType());
        vo.setModelType(entity.getModelType());
        vo.setDescription(entity.getDescription());
        return vo;
    }

    /**
     * 查询模型列表
     */
    default List<SelectModelVO> selectModelList(Boolean filterEmbedding) {
        LambdaQueryWrapperX<AiModelDO> wrapper = new LambdaQueryWrapperX<AiModelDO>()
                .eq(AiModelDO::getDeleted, false);
        if (filterEmbedding != null && filterEmbedding) {
            wrapper.ne(AiModelDO::getModelType, 0);
        }
        wrapper.orderByAsc(AiModelDO::getType);
        return selectList(wrapper).stream().map(entity -> {
            SelectModelVO vo = new SelectModelVO();
            vo.setId(entity.getId());
            vo.setName(entity.getName());
            vo.setRename(entity.getRename());
            vo.setType(entity.getType());
            vo.setModelType(entity.getModelType());
            vo.setDescription(entity.getDescription());
            return vo;
        }).toList();
    }

    /**
     * 软删除模型
     */
    default Boolean deleteModelById(List<Long> ids) {
        update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AiModelDO>()
                .in(AiModelDO::getId, ids)
                .set(AiModelDO::getDeleted, true));
        return true;
    }

    /**
     * 查询用户创建的模型 ID 列表
     */
    default List<Long> selectIdsByCreator(String creator) {
        return selectList(new LambdaQueryWrapperX<AiModelDO>()
                .eq(AiModelDO::getCreator, creator)
                .eq(AiModelDO::getDeleted, false))
                .stream().map(AiModelDO::getId).toList();
    }

    /**
     * 查询当前租户下的模型 ID 列表
     */
    default List<Long> selectIdsByTenant() {
        return selectList(new LambdaQueryWrapperX<AiModelDO>()
                .select(AiModelDO::getId)
                .eq(AiModelDO::getDeleted, false))
                .stream().map(AiModelDO::getId).toList();
    }

    /**
     * 批量查询模型 ID 对应的归属租户
     *
     * 注意：跨租户使用，调用方需通过 {@link cn.iocoder.yudao.framework.tenant.core.util.TenantUtils#executeIgnore}
     * 绕过租户插件，否则会被自动追加 tenant_id 过滤。
     */
    default Map<Long, Long> selectTenantIdMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return selectList(new LambdaQueryWrapperX<AiModelDO>()
                .select(AiModelDO::getId, AiModelDO::getTenantId)
                .in(AiModelDO::getId, ids)
                .eq(AiModelDO::getDeleted, false))
                .stream().collect(Collectors.toMap(AiModelDO::getId, AiModelDO::getTenantId));
    }

    /**
     * 查询全部模型 ID 列表（跨租户由调用方通过 {@link cn.iocoder.yudao.framework.tenant.core.util.TenantUtils#executeIgnore} 控制）
     */
    default List<Long> selectAllIds() {
        return selectList(new LambdaQueryWrapperX<AiModelDO>()
                .select(AiModelDO::getId)
                .eq(AiModelDO::getDeleted, false))
                .stream().map(AiModelDO::getId).toList();
    }

}
