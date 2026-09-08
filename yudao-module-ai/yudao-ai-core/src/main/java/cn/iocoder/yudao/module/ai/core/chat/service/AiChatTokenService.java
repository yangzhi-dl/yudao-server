package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTokenDO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.TokenStatisticsVO;

public interface AiChatTokenService {

    void insert(AiChatTokenDO token);

    TokenStatisticsVO selectHourlyTokenByYearAndUser(Integer year);

    TokenStatisticsVO selectDailyTokenByYearAndUser(Integer year);

}
