package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.module.ai.common.utils.ResultUtil;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTokenDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatTokenMapper;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.TokenStatistics;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.TokenStatisticsVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Service
@Slf4j
public class AiChatTokenServiceImpl implements AiChatTokenService {

    private final AiChatTokenMapper tokenMapper;

    public AiChatTokenServiceImpl(AiChatTokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    @Override
    public void insert(AiChatTokenDO token) {
        tokenMapper.insert(token);
    }

    @Override
    public TokenStatisticsVO selectHourlyTokenByYearAndUser(Integer year) {
        String loginUserId = String.valueOf(getLoginUserId());
        List<TokenStatistics> statisticsList = tokenMapper.selectHourlyTokenByYearAndUser(year, loginUserId);

        // 获取当年天数
        int days = ResultUtil.isLeapYear(year) ? 366 : 365;
        int totalHours = days * 24;

        // 初始化完整小时列表
        List<LocalDateTime> dates = new ArrayList<>(totalHours);
        List<Long> tokens = new ArrayList<>(totalHours);

        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        Map<LocalDateTime, Long> statMap = new HashMap<>();

        for (TokenStatistics s : statisticsList) {
            statMap.put(s.getDate(), s.getData());
        }

        for (int i = 0; i < totalHours; i++) {
            LocalDateTime dt = start.plusHours(i);
            dates.add(dt);
            tokens.add(statMap.getOrDefault(dt, 0L));  // 没有数据的设为0
        }

        return TokenStatisticsVO.builder()
                .dates(dates)
                .tokens(tokens)
                .build();
    }

    @Override
    public TokenStatisticsVO selectDailyTokenByYearAndUser(Integer year) {
        String loginUserId = String.valueOf(getLoginUserId());

        // 确定有效日期范围（避免生成全年数据）
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = year.equals(LocalDate.now().getYear())
                ? LocalDate.now()
                : LocalDate.of(year, 12, 31);

        // 查询数据库 → 转换为 Map<时间戳, 数值> 提升查找性能
        List<TokenStatistics> statisticsList = tokenMapper.selectDailyTokenByYearAndUser(year, loginUserId);
        Map<Long, Long> statMap = statisticsList.stream()
                .collect(Collectors.toMap(
                        s -> s.getDate().toLocalDate().toEpochDay(), // 用 epochDay 替代 LocalDate 作 key
                        TokenStatistics::getData,
                        (v1, v2) -> v1 // 处理可能的重复键
                ));

        // 找出有数据的有效区间（含前后1天缓冲）
        int[] range = findEffectiveRange(statMap, startDate.toEpochDay(), endDate.toEpochDay());
        if (range == null) {
            return TokenStatisticsVO.builder()
                    .tokens(List.of()).dates(List.of()).build();
        }

        // 4. 只生成有效区间内的数据（避免无意义的0值填充）
        List<LocalDateTime> resultDates = new ArrayList<>();
        List<Long> resultTokens = new ArrayList<>();

        for (long day = range[0]; day <= range[1]; day++) {
            resultDates.add(LocalDate.ofEpochDay(day).atStartOfDay());
            resultTokens.add(statMap.getOrDefault(day, 0L));
        }

        return TokenStatisticsVO.builder()
                .dates(resultDates)
                .tokens(resultTokens)
                .build();
    }

    /**
     * 查找有效数据区间 [start, end]，前后各保留1天缓冲
     * @return 包含起止 epochDay 的数组，无有效数据时返回 null
     */
    private int[] findEffectiveRange(Map<Long, Long> statMap, long startDay, long endDay) {
        int firstNonZero = -1, lastNonZero = -1;

        // 单次遍历找首尾非零位置（相对起始日的偏移）
        for (long day = startDay, idx = 0; day <= endDay; day++, idx++) {
            if (statMap.getOrDefault(day, 0L) != 0L) {
                if (firstNonZero == -1) firstNonZero = (int) idx;
                lastNonZero = (int) idx;
            }
        }

        // 无有效数据
        if (firstNonZero == -1) return null;

        // 计算缓冲后的绝对 epochDay 范围
        long bufferStart = Math.max(startDay, startDay + firstNonZero - 1);
        long bufferEnd = Math.min(endDay, startDay + lastNonZero + 1);

        return new int[]{(int) bufferStart, (int) bufferEnd};
    }

}
