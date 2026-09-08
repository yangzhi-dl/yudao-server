package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.AgentPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectAgentVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 智能体 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiAgentMapper extends BaseMapperX<AiAgentDO> {

    /**
     * 查询智能体列表（JOIN 模型表 - XML）
     */
    List<SelectAgentVO> selectAgentList();

    /**
     * 分页查询（XML - 需要 autoResultMap 处理 JSON 字段）
     */
    Page<AiAgentDO> pageQuery(@Param("page") Page<AiAgentDO> page, @Param("dto") AgentPageQueryDTO dto);

    /**
     * 插入（XML - 需要 JacksonTypeHandler 处理 JSON 字段）
     */
    int insertAgent(AiAgentDO aiAgentDO);

    /**
     * 更新（XML - 需要 JacksonTypeHandler 处理 JSON 字段）
     */
    int updateAgent(AiAgentDO aiAgentDO);

    /**
     * 按 ID 查询智能体
     */
    default AiAgentDO selectAgentById(Long id) {
        return selectOne(new LambdaQueryWrapperX<AiAgentDO>()
                .eq(AiAgentDO::getId, id)
                .eq(AiAgentDO::getDeleted, false));
    }

    /**
     * 软删除智能体
     */
    default Boolean delete(List<Long> ids) {
        update(new LambdaUpdateWrapper<AiAgentDO>()
                .in(AiAgentDO::getId, ids)
                .set(AiAgentDO::getDeleted, true));
        return true;
    }

    /**
     * 增加使用次数
     */
    default Boolean increaseUsageCount(Long id) {
        update(new LambdaUpdateWrapper<AiAgentDO>()
                .eq(AiAgentDO::getId, id)
                .setSql("usage_count = usage_count + 1"));
        return true;
    }

    /**
     * 查询用户创建的智能体 ID 列表
     */
    default List<Long> selectIdsByCreator(String creator) {
        return selectList(new LambdaQueryWrapperX<AiAgentDO>()
                .eq(AiAgentDO::getCreator, creator)
                .eq(AiAgentDO::getDeleted, false))
                .stream().map(AiAgentDO::getId).toList();
    }

    /**
     * 查询当前租户下的智能体 ID 列表
     */
    default List<Long> selectIdsByTenant() {
        return selectList(new LambdaQueryWrapperX<AiAgentDO>()
                .select(AiAgentDO::getId)
                .eq(AiAgentDO::getDeleted, false))
                .stream().map(AiAgentDO::getId).toList();
    }

    /**
     * 批量查询智能体 ID 对应的归属租户
     *
     * 注意：跨租户使用，调用方需通过 {@link cn.iocoder.yudao.framework.tenant.core.util.TenantUtils#executeIgnore}
     * 绕过租户插件，否则会被自动追加 tenant_id 过滤。
     */
    default Map<Long, Long> selectTenantIdMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return selectList(new LambdaQueryWrapperX<AiAgentDO>()
                .select(AiAgentDO::getId, AiAgentDO::getTenantId)
                .in(AiAgentDO::getId, ids)
                .eq(AiAgentDO::getDeleted, false))
                .stream().collect(Collectors.toMap(AiAgentDO::getId, AiAgentDO::getTenantId));
    }

    /**
     * 查询全部智能体 ID 列表（跨租户由调用方通过 {@link cn.iocoder.yudao.framework.tenant.core.util.TenantUtils#executeIgnore} 控制）
     */
    default List<Long> selectAllIds() {
        return selectList(new LambdaQueryWrapperX<AiAgentDO>()
                .select(AiAgentDO::getId)
                .eq(AiAgentDO::getDeleted, false))
                .stream().map(AiAgentDO::getId).toList();
    }

}
