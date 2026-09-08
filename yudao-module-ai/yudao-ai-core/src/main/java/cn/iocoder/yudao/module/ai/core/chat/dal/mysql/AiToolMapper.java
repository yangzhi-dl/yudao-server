package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelToolDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ToolPageQueryDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * AI 工具 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiToolMapper extends BaseMapperX<ModelToolDO> {

    /**
     * 分页查询（XML - 动态 WHERE + JacksonTypeHandler）
     */
    Page<ModelToolDO> pageQuery(@Param("page") Page<ModelToolDO> page, @Param("dto") ToolPageQueryDTO dto);

    /**
     * 更新在线状态（XML）
     */
    void updateOnlineStatus(Long id, Boolean isOnline);

    /**
     * 根据 ID 列表获取在线的工具（XML - 需要 autoResultMap 处理 JSON 字段）
     */
    List<ModelToolDO> getOnlineToolsByIds(List<Long> ids);

    /**
     * 按 ID 查询工具
     */
    default ModelToolDO selectById(Long id) {
        return selectOne(new LambdaQueryWrapperX<ModelToolDO>()
                .eq(ModelToolDO::getId, id));
    }

    /**
     * 检查工具名称唯一性
     */
    default List<String> hasUniqueNames(List<String> names) {
        return selectList(new LambdaQueryWrapperX<ModelToolDO>()
                .in(ModelToolDO::getName, names))
                .stream().map(ModelToolDO::getName).toList();
    }

    /**
     * 获取在线工具数量
     */
    default Integer getToolSize() {
        return Math.toIntExact(selectCount(new LambdaQueryWrapperX<ModelToolDO>()
                .eq(ModelToolDO::getIsOnline, true)));
    }

    /**
     * 删除工具（保留最后一次删除的副本）
     * <p>
     * 流程：先物理删除同名+同创建者的旧已删除记录 → 再逻辑删除当前记录
     * 确保 uk_name_creator_deleted(name, creator, deleted) 唯一键不冲突
     */
    default Boolean delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }

        // 1. 查询要删除的记录，获取 name
        List<ModelToolDO> records = selectList(
                new LambdaQueryWrapperX<ModelToolDO>()
                        .in(ModelToolDO::getId, ids)
                        .select(ModelToolDO::getName)
        );

        List<String> names = records.stream()
                .map(ModelToolDO::getName)
                .distinct()
                .toList();

        // 2. 物理删除已存在的同名已删除记录（避免 uk_name_deleted 唯一键冲突）
        if (!names.isEmpty()) {
            deleteOldDeletedByName(names);
        }

        // 3. 逻辑删除当前记录（@TableLogic → UPDATE SET deleted = 1）
        return delete(new LambdaQueryWrapperX<ModelToolDO>()
                .in(ModelToolDO::getId, ids)) > 0;
    }

    /**
     * 物理删除同名已删除记录（绕过 @TableLogic，清理 deleted=1 的旧副本）
     */
    void deleteOldDeletedByName(@Param("names") List<String> names);

    /**
     * 更新工具信息
     */
    default Boolean update(ModelToolDO modelToolDO) {
        return updateById(modelToolDO) > 0;
    }

}
