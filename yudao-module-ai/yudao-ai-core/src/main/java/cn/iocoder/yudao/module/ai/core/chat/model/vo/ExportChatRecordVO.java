package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportChatRecordVO {

    private String url;

    private LocalDateTime exportTime;

}
