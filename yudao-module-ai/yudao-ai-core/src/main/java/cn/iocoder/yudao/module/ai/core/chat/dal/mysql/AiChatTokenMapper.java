package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTokenDO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.TokenStatistics;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 对话 Token Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiChatTokenMapper extends BaseMapperX<AiChatTokenDO> {

    /**
     * 按年和用户查询每小时 Token 统计
     */
    List<TokenStatistics> selectHourlyTokenByYearAndUser(Integer year, String creator);

    /**
     * 按年和用户查询每天 Token 统计
     */
    List<TokenStatistics> selectDailyTokenByYearAndUser(Integer year, String creator);

}
