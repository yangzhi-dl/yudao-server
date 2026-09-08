package cn.iocoder.yudao.module.ai.common.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long[] ids;

    private Integer type;

    private String updater;

    private LocalDateTime updateTime;


}
