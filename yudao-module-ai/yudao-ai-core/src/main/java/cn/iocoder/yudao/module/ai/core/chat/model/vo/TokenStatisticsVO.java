package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatisticsVO {

    private List<LocalDateTime> dates;

    /**
     * 每小时 token 使用量
     */
    private List<Long> tokens;

}
