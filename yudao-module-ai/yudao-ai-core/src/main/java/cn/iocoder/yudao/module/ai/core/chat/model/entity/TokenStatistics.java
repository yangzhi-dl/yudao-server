package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatistics {

    private LocalDateTime date;

    /**
     * 每小时 token 使用量
     */
    private Long data;

}
