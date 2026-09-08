package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.module.ai.core.chat.enums.AgentStatus;

import java.util.Map;

public interface AiProfessionAppService {

    void updateStatus(Long agentId, AgentStatus status);

    Map<String, Integer> getGlobalStatistics();

}
