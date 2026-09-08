package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import cn.iocoder.yudao.module.ai.core.chat.enums.McpTransmissionProtocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpConfig {

    private String url;

    private String endpoint;

    private McpTransmissionProtocol tp;

    private String name;

    private String version;

    private Map<String, String> header;

    private Integer connectTimeout;

}
