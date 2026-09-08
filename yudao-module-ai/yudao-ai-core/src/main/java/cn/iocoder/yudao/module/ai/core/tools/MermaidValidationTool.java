package cn.iocoder.yudao.module.ai.core.tools;

import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Mermaid 语法校验器 —— 调用原生 mermaid.js 解析器进行语法校验。
 * <p>
 * 依赖 Node.js 校验服务（mermaid-validator），通过 HTTP 调用 mermaid.parse() API。
 * 校验结果 100% 准确，与 mermaid.live 完全一致。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class MermaidValidationTool implements AITool {

    private final ObjectMapper objectMapper;

    /** Node.js 校验服务地址，配置于 application.yaml 的 iims.tools.mermaid-validation.uri */
    @Value("${iims.tools.mermaid-validation.uri}")
    private String validatorUrl;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    // ==================== AITool 接口 ====================

    @Override public Object getToolInstance() { return this; }
    @Override public String getName() { return "mermaid-validation-tools"; }
    @Override public String getTitle() { return "Mermaid语法校验器"; }

    @Override
    public String getDescription() {
        return "调用原生 mermaid.js 解析器校验 Mermaid 图表代码语法，校验结果与 mermaid.live 100% 一致。"
                + "支持 28 种官方图表类型。";
    }

    // ==================== 主入口 ====================

    @Tool(description =
        "校验 Mermaid 图表代码语法。调用原生 mermaid.js 解析器进行完整语法校验（与 mermaid.live 在线编辑器使用同一套解析器），"
        + "返回结构化校验结果（是否通过、解析出的图类型或错误信息）。"
        + "在生成 Mermaid 图表代码后调用此方法，根据返回结果修正代码直到 valid=true 为止。")
    public String validateMermaidCode(
            @ToolParam(description = "完整的 Mermaid 图表代码，从图类型声明行开始，如 'flowchart TD\\n  A --> B'") String mermaidCode) {

        if (mermaidCode == null || mermaidCode.trim().isEmpty()) {
            return errorResult("Mermaid 代码为空，无法校验");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of("code", mermaidCode.trim()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(validatorUrl + "/api/validate"))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return errorResult("校验服务返回异常状态码: " + response.statusCode());
            }

            return response.body();

        } catch (java.net.ConnectException e) {
            return errorResult("Mermaid 校验服务连接失败 (" + validatorUrl
                    + ")。请确认 Node.js 服务已启动：cd mermaid-validator && npm start");
        } catch (java.io.IOException e) {
            return errorResult("调用 Mermaid 校验服务网络异常: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorResult("校验请求被中断");
        } catch (Exception e) {
            return errorResult("校验过程发生异常: " + e.getMessage());
        }
    }

    private String errorResult(String message) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "valid", false,
                    "error", message
            ));
        } catch (Exception e) {
            return "{\"valid\":false,\"error\":\"" + message + "\"}";
        }
    }
}
