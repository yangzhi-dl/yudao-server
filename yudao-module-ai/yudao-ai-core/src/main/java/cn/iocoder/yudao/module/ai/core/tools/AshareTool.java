package cn.iocoder.yudao.module.ai.core.tools;

import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * A股股票行情数据查询工具 —— 调用 ashare-service 获取实时行情与技术指标。
 * <p>
 * 依赖 Node.js 行情服务（ashare-service），基于腾讯/新浪免费数据源。
 * 支持 A 股指数及个股的日线、周线、月线、分钟线行情查询，以及 12 种常用技术指标计算。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class AshareTool implements AITool {

    private final ObjectMapper objectMapper;

    /** Node.js 行情服务地址，配置于 application.yaml 的 iims.tools.ashare.uri */
    @Value("${iims.tools.ashare.uri}")
    private String serviceUrl;

    /** 工具是否启用，配置于 application.yaml 的 iims.tools.ashare.enabled，默认启用 */
    @Value("${iims.tools.ashare.enabled:true}")
    private boolean enabled;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    // ==================== AITool 接口 ====================

    @Override public Object getToolInstance() { return this; }
    @Override public String getName() { return "ashare-tools"; }
    @Override public String getTitle() { return "A股行情查询"; }
    @Override public boolean isEnabled() { return enabled; }

    @Override
    public String getDescription() {
        return "查询 A 股（上证/深证）指数及个股的实时行情数据，支持日线/周线/月线/分钟线 K 线数据，"
                + "以及 MACD、KDJ、RSI、BOLL、WR、BIAS 等 12 种常用技术指标计算。"
                + "数据源来自腾讯/新浪免费接口。";
    }

    // ==================== 行情查询 ====================

    @Tool(description =
        "获取 A 股股票行情 K 线数据。支持日线(1d)、周线(1w)、月线(1M)、1分钟(1m)、5分钟(5m)、15分钟(15m)、30分钟(30m)、60分钟(60m)。"
        + "代码格式兼容 sh000001(上证指数)、000001.XSHG、sz399006(深证成指)、399006.XSHE、sh600519(贵州茅台) 等。"
        + "返回包含时间、开盘价、收盘价、最高价、最低价、成交量的 K 线数据列表。")
    public String getStockPrice(
            @ToolParam(description = "股票代码，支持多种格式：sh000001、000001.XSHG、sz399006、399006.XSHE、sh600519、600519.XSHG")
            String code,
            @ToolParam(description = "K线周期：1d(日线)、1w(周线)、1M(月线)、1m(1分钟)、5m(5分钟)、15m、30m、60m，默认 1d")
            String frequency,
            @ToolParam(description = "获取数据条数，默认 10，最大建议 200")
            String count,
            @ToolParam(description = "结束日期，格式 YYYY-MM-DD，不传则获取最新数据")
            String endDate) {

        if (code == null || code.trim().isEmpty()) {
            return errorResult("股票代码不能为空");
        }

        try {
            String freq = (frequency == null || frequency.trim().isEmpty()) ? "1d" : frequency.trim();
            String cnt = (count == null || count.trim().isEmpty()) ? "10" : count.trim();
            String end = (endDate == null || endDate.trim().isEmpty()) ? "" : endDate.trim();

            String query = String.format("code=%s&frequency=%s&count=%s",
                    URLEncoder.encode(code.trim(), StandardCharsets.UTF_8),
                    URLEncoder.encode(freq, StandardCharsets.UTF_8),
                    URLEncoder.encode(cnt, StandardCharsets.UTF_8));
            if (!end.isEmpty()) {
                query += "&end_date=" + URLEncoder.encode(end, StandardCharsets.UTF_8);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceUrl + "/api/price?" + query))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return errorResult("行情服务返回异常状态码: " + response.statusCode());
            }

            return response.body();

        } catch (java.net.ConnectException e) {
            return errorResult("A股行情服务连接失败 (" + serviceUrl
                    + ")。请确认 Node.js 服务已启动：cd ashare-service && npm start");
        } catch (java.io.IOException e) {
            return errorResult("调用行情服务网络异常: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorResult("行情请求被中断");
        } catch (Exception e) {
            return errorResult("查询过程发生异常: " + e.getMessage());
        }
    }

    @Tool(description =
        "获取 A 股股票的技术指标。支持 MA(均线)、MACD、KDJ、RSI、BOLL(布林带)、WR(威廉指标)、BIAS(乖离率)、"
        + "PSY(心理线)、CCI(商品通道指数)、ATR(真实波幅)、BBI(多空指标)、DMI(动向指标)。"
        + "默认返回 MA、BOLL、MACD 三个指标。需要较多历史数据，建议 count >= 60。")
    public String getStockIndicators(
            @ToolParam(description = "股票代码，支持多种格式：sh000001、000001.XSHG、sz399006、399006.XSHE")
            String code,
            @ToolParam(description = "K线周期：1d(日线)、1w(周线)、1M(月线)，默认 1d")
            String frequency,
            @ToolParam(description = "数据条数，默认 120，建议 >= 60 以保证指标精度")
            String count,
            @ToolParam(description = "指标名称列表，逗号分隔。可选: MA, MACD, KDJ, RSI, BOLL, WR, BIAS, PSY, CCI, ATR, BBI, DMI。默认 MA,BOLL,MACD")
            String indicators) {

        if (code == null || code.trim().isEmpty()) {
            return errorResult("股票代码不能为空");
        }

        try {
            String freq = (frequency == null || frequency.trim().isEmpty()) ? "1d" : frequency.trim();
            String cnt = (count == null || count.trim().isEmpty()) ? "120" : count.trim();
            String ind = (indicators == null || indicators.trim().isEmpty()) ? "MA,BOLL,MACD" : indicators.trim();

            String query = String.format("code=%s&frequency=%s&count=%s&indicators=%s",
                    URLEncoder.encode(code.trim(), StandardCharsets.UTF_8),
                    URLEncoder.encode(freq, StandardCharsets.UTF_8),
                    URLEncoder.encode(cnt, StandardCharsets.UTF_8),
                    URLEncoder.encode(ind, StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceUrl + "/api/indicators?" + query))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return errorResult("行情服务返回异常状态码: " + response.statusCode());
            }

            return response.body();

        } catch (java.net.ConnectException e) {
            return errorResult("A股行情服务连接失败 (" + serviceUrl
                    + ")。请确认 Node.js 服务已启动：cd ashare-service && npm start");
        } catch (java.io.IOException e) {
            return errorResult("调用行情服务网络异常: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorResult("行情请求被中断");
        } catch (Exception e) {
            return errorResult("查询过程发生异常: " + e.getMessage());
        }
    }

    // ==================== 行情+指标综合查询 ====================

    @Tool(description =
        "获取 A 股行情 K 线数据及全部 12 种技术指标的完整序列（用于图表绘制和深度分析）。"
        + "返回原始 K 线数据 + MACD、KDJ、RSI、BOLL、WR、BIAS、PSY、CCI、ATR、BBI、DMI 的完整时间序列。"
        + "建议 count >= 60 以保证指标精度。")
    public String getPriceWithIndicators(
            @ToolParam(description = "股票代码，支持多种格式：sh000001、000001.XSHG、sz399006、399006.XSHE")
            String code,
            @ToolParam(description = "K线周期：1d(日线)、1w(周线)、1M(月线)，默认 1d")
            String frequency,
            @ToolParam(description = "数据条数，默认 120，建议 >= 60 以保证指标精度")
            String count) {

        if (code == null || code.trim().isEmpty()) {
            return errorResult("股票代码不能为空");
        }

        try {
            String freq = (frequency == null || frequency.trim().isEmpty()) ? "1d" : frequency.trim();
            String cnt = (count == null || count.trim().isEmpty()) ? "120" : count.trim();

            String query = String.format("code=%s&frequency=%s&count=%s",
                    URLEncoder.encode(code.trim(), StandardCharsets.UTF_8),
                    URLEncoder.encode(freq, StandardCharsets.UTF_8),
                    URLEncoder.encode(cnt, StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceUrl + "/api/price-with-indicators?" + query))
                    .timeout(REQUEST_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return errorResult("行情服务返回异常状态码: " + response.statusCode());
            }

            return response.body();

        } catch (java.net.ConnectException e) {
            return errorResult("A股行情服务连接失败 (" + serviceUrl
                    + ")。请确认 Node.js 服务已启动：cd ashare-service && npm start");
        } catch (java.io.IOException e) {
            return errorResult("调用行情服务网络异常: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorResult("行情请求被中断");
        } catch (Exception e) {
            return errorResult("查询过程发生异常: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private String errorResult(String message) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "error", message
            ));
        } catch (Exception e) {
            return "{\"error\":\"" + message + "\"}";
        }
    }
}