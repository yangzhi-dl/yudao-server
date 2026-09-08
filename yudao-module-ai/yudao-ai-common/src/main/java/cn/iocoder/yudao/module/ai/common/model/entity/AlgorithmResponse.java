package cn.iocoder.yudao.module.ai.common.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlgorithmResponse {

    private Integer code;

    private String message;

    private Object data;

}
