package cn.iocoder.yudao.module.ai.knowledge.wiki.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.common.model.entity.DeletedStatus;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto.FindAccessibleWikiPageListDTO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto.FindWikiPageListDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface WikiMapper extends BaseMapperX<Wiki> {

    // ========== XML（需要 JacksonTypeHandler 或动态 SQL） ==========

    /** 批量删除知识库（软删除） */
    int deleteWikiByIds(DeletedStatus deletedStatus);

    /** 更新知识库（含 settings JSON 字段） */
    int updateWiki(Wiki wiki);

    /** 按 ID 查询知识库（含 settings JSON 字段反序列化） */
    Wiki findWikiById(Long id);

    // ========== 原生默认方法（利用 autoResultMap 自动处理 settings JSON） ==========

    /** 管理端分页查询（ACL 数据权限自动过滤） */
    default Page<Wiki> pageQuery(Page<Wiki> page, FindWikiPageListDTO dto) {
        LambdaQueryWrapperX<Wiki> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Wiki::getDeleted, 0);
        wrapper.likeIfPresent(Wiki::getTitle, dto.getTitle());
        wrapper.orderByDesc(Wiki::getWeight);
        wrapper.orderByDesc(Wiki::getCreateTime);
        return selectPage(page, wrapper);
    }

    /** 已发布知识库分页查询（ACL 数据权限自动过滤） */
    default Page<Wiki> pagePublishQuery(Page<Wiki> page, FindAccessibleWikiPageListDTO dto) {
        LambdaQueryWrapperX<Wiki> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Wiki::getDeleted, 0);
        wrapper.likeIfPresent(Wiki::getTitle, dto.getTitle());
        wrapper.eqIfPresent(Wiki::getType, dto.getType());
        wrapper.orderByDesc(Wiki::getWeight);
        wrapper.orderByDesc(Wiki::getCreateTime);
        return selectPage(page, wrapper);
    }

    /** 查询最大权重 */
    default Wiki selectMaxWeight() {
        LambdaQueryWrapperX<Wiki> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Wiki::getDeleted, 0);
        wrapper.orderByDesc(Wiki::getWeight);
        wrapper.last("limit 1");
        List<Wiki> list = selectList(wrapper);
        return CollUtil.isEmpty(list) ? null : list.getFirst();
    }

    /** 按创建者查询知识库 ID 列表（未删除） */
    default List<Long> selectIdsByCreator(String creator) {
        LambdaQueryWrapperX<Wiki> wrapper = new LambdaQueryWrapperX<>();
        wrapper.select(Wiki::getId);
        wrapper.eq(Wiki::getCreator, creator);
        wrapper.eq(Wiki::getDeleted, 0);
        return selectList(wrapper).stream().map(Wiki::getId).toList();
    }

    /** 查询当前租户下的知识库 ID 列表（未删除） */
    default List<Long> selectIdsByTenant() {
        LambdaQueryWrapperX<Wiki> wrapper = new LambdaQueryWrapperX<>();
        wrapper.select(Wiki::getId);
        wrapper.eq(Wiki::getDeleted, 0);
        return selectList(wrapper).stream().map(Wiki::getId).toList();
    }

    /** 查询全部知识库 ID 列表（跨租户由调用方通过 TenantUtils#executeIgnore 控制） */
    default List<Long> selectAllIds() {
        return selectList(new LambdaQueryWrapperX<Wiki>()
                .select(Wiki::getId)
                .eq(Wiki::getDeleted, 0))
                .stream().map(Wiki::getId).toList();
    }

    /**
     * 批量查询知识库 ID 对应的归属租户
     *
     * 注意：跨租户使用，调用方需通过 {@link cn.iocoder.yudao.framework.tenant.core.util.TenantUtils#executeIgnore}
     * 绕过租户插件，否则会被自动追加 tenant_id 过滤。
     */
    default Map<Long, Long> selectTenantIdMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return selectList(new LambdaQueryWrapperX<Wiki>()
                .select(Wiki::getId, Wiki::getTenantId)
                .in(Wiki::getId, ids)
                .eq(Wiki::getDeleted, 0))
                .stream().collect(Collectors.toMap(Wiki::getId, Wiki::getTenantId));
    }

    /** 按 ID 列表查询知识库 */
    default List<Wiki> selectWikiByIds(List<Long> ids) {
        LambdaQueryWrapperX<Wiki> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Wiki::getDeleted, 0);
        wrapper.in(Wiki::getId, ids);
        return selectList(wrapper);
    }

    /** 按 ID 列表分页查询知识库 */
    default Page<Wiki> selectWikiPageByIds(Page<Wiki> page, List<Long> ids) {
        LambdaQueryWrapperX<Wiki> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(Wiki::getDeleted, 0);
        wrapper.in(Wiki::getId, ids);
        return selectPage(page, wrapper);
    }
}
