package cn.iocoder.yudao.module.ai.common.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.common.dal.dataobject.UsageRecordDO;
import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 使用记录 Mapper
 *
 * @author yudao
 */
@Mapper
public interface UsageRecordMapper extends BaseMapperX<UsageRecordDO> {

    /**
     * 判断指定类型的记录是否存在
     */
    default Boolean existsByTypeAndObjectId(UsageRecordType type, Long objectId, String creator) {
        return selectCount(new LambdaQueryWrapperX<UsageRecordDO>()
                .eq(UsageRecordDO::getType, type).eq(UsageRecordDO::getCreator, creator)
                .eq(UsageRecordDO::getObjectId, objectId)) > 0;
    }

    /**
     * 更新时间
     */
    default void updateTime(LocalDateTime updateTime, UsageRecordType type, Long objectId, String creator) {
        update(new LambdaUpdateWrapper<UsageRecordDO>()
                .eq(UsageRecordDO::getType, type)
                .eq(UsageRecordDO::getObjectId, objectId)
                .eq(UsageRecordDO::getCreator, creator)
                .set(UsageRecordDO::getUpdateTime, updateTime));
    }

    /**
     * 查询用户指定类型的记录列表
     */
    default List<UsageRecordDO> selectUserListByType(UsageRecordType type, String creator, Integer limit) {
        LambdaQueryWrapperX<UsageRecordDO> wrapper = new LambdaQueryWrapperX<UsageRecordDO>()
                .eq(UsageRecordDO::getType, type).eq(UsageRecordDO::getCreator, creator)
                .orderByDesc(UsageRecordDO::getUpdateTime);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        return selectList(wrapper);
    }

}
