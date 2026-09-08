package cn.iocoder.yudao.module.ai.core.tools;

import cn.iocoder.yudao.module.ai.common.properties.DzintAuthProperties;
import cn.iocoder.yudao.module.ai.common.utils.DzintAuthTokenUtil;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

/**
 * 票据识别与查验工具
 * 调用DZINT开放API实现票据图片的自动分类识别和要素提取
 *
 */
@Slf4j
@Component
public class InvoiceTool implements AITool {

    private static final String RECOGNITION_PATH = "/open-api/v1/invoice/recognition";
    private static final String VERIFIER_PATH = "/open-api/v1/invoice/verifier";

    private final RestTemplate restTemplate;
    private final DzintAuthTokenUtil dzintAuthTokenUtil;
    private final DzintAuthProperties dzintAuthProperties;
    private final ObjectMapper objectMapper;
    private final FileService fileService;

    /** 工具是否启用，配置于 application.yaml 的 iims.tools.invoice.enabled，默认启用 */
    @Value("${iims.tools.invoice.enabled:true}")
    private boolean enabled;

    public InvoiceTool(RestTemplate restTemplate,
                       DzintAuthTokenUtil dzintAuthTokenUtil,
                       DzintAuthProperties dzintAuthProperties, FileService fileService) {
        this.restTemplate = restTemplate;
        this.dzintAuthTokenUtil = dzintAuthTokenUtil;
        this.dzintAuthProperties = dzintAuthProperties;
        this.fileService = fileService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "invoice-tools";
    }

    @Override
    public String getTitle() {
        return "票据识别与查验工具箱";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getDescription() {
        return "上传票据图片，自动识别票据类型（增值税发票、电子发票、交通发票等）并提取票据要素信息（金额、日期、号码等）";
    }

    /**
     * 票据识别：上传票据图片进行类型分类和要素提取
     *
     * @param fileId 票据图片文件ID
     * @return 识别结果（JSON格式，包含票据类型和要素信息）
     */
    @Tool(description = "上传票据进行识别，自动分类票据类型（增值税发票/电子发票/交通发票/海关税票/机动车发票/通用机打发票等）并提取票据要素信息（金额、日期、发票号码、税额等）")
    public String recognizeInvoice(@ToolParam(description = "票据文件ID") Long fileId,
                                   @ToolParam(description = "文件类型（image/jpeg、image/jpg、image/png、application/pdf、application/ofd）") String fileType) {
        return callInvoiceApi(fileId, fileType);
    }

    /**
     * 票据查验：根据发票要素信息验证票据真伪
     *
     * @param invoiceNumber      发票号码（必填）
     * @param invoiceTaxExclusive 发票金额（必填）
     * @param invoiceIssueDate   发票日期（必填）
     * @param invoiceCheckCode   校验码（必填）
     * @param invoiceCode        发票代码（选填）
     * @return 查验结果
     */
    @Tool(description = "根据发票号码、金额、日期、校验码等要素信息查验票据真伪")
    public String verifyInvoice(@ToolParam(description = "发票号码（必填）") String invoiceNumber,
                                @ToolParam(description = "发票金额（必填）") String invoiceTaxExclusive,
                                @ToolParam(description = "发票日期，格式YYYYMMDD（必填）") String invoiceIssueDate,
                                @ToolParam(description = "校验码（选填）", required = false) String invoiceCheckCode,
                                @ToolParam(description = "发票代码（选填）", required = false) String invoiceCode) {
        return callVerifierApi(invoiceNumber, invoiceTaxExclusive, invoiceIssueDate, invoiceCheckCode, invoiceCode);
    }

    /**
     * 调用DZINT票据查验API
     */
    private String callVerifierApi(String invoiceNumber, String invoiceTaxExclusive,
                                   String invoiceIssueDate, String invoiceCheckCode,
                                   String invoiceCode) {
        try {
            // 构建鉴权头
            HttpHeaders headers = buildAuthHeaders();

            // 构建查询参数
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(dzintAuthProperties.getUri() + VERIFIER_PATH)
                    .queryParam("invoiceNumber", invoiceNumber)
                    .queryParam("invoiceTaxExclusive", invoiceTaxExclusive)
                    .queryParam("invoiceIssueDate", invoiceIssueDate)
                    .queryParam("invoiceCheckCode", invoiceCheckCode);

            if (invoiceCode != null && !invoiceCode.isEmpty()) {
                uriBuilder.queryParam("invoiceCode", invoiceCode);
            }

            uriBuilder.queryParam("source", "IIMS");

            String url = uriBuilder.build().toUriString();
            log.debug("票据查验请求: url={}, invoiceNumber={}", url, invoiceNumber);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("票据查验成功: invoiceNumber={}", invoiceNumber);
                return formatVerifierResponse(response.getBody());
            } else {
                log.warn("票据查验失败: invoiceNumber={}, status={}", invoiceNumber, response.getStatusCode());
                return "票据查验失败：HTTP " + response.getStatusCode().value();
            }

        } catch (Exception e) {
            log.error("票据查验请求异常: invoiceNumber={}", invoiceNumber, e);
            return "票据查验失败：" + e.getMessage();
        }
    }

    /**
     * 调用DZINT票据识别API
     */
    private String callInvoiceApi(Long fileId, String fileType) {
        String fileEnd = getFileEnd(fileType);
        if (Objects.isNull(fileEnd)) {
            return "传入的文件类型不满足工具的调用，应该使用：[image/jpeg, image/jpg, image/png, application/pdf, application/ofd]，其中的一种";
        }
        FileDO file = fileService.getFile(fileId);

        try {
            byte[] fileBytes = fileService.getFileContent(file.getConfigId(), file.getPath());
            // 构建鉴权头（含multipart类型）
            HttpHeaders headers = buildAuthHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 构建 multipart 请求体 - 使用 ByteArrayResource
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 关键：使用 ByteArrayResource 而不是 MultipartFile
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return String.format("invoice_%d.%s", fileId, fileEnd);
                }
            };
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送请求
            String url = dzintAuthProperties.getUri() + RECOGNITION_PATH;
            log.debug("票据识别请求: url={}, clientId={}, fileId={}", url, dzintAuthTokenUtil.getClientId(), fileId);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            // 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("票据识别成功: fileId={}", fileId);
                return formatResponse(response.getBody());
            } else {
                log.warn("票据识别失败: fileId={}, status={}", fileId, response.getStatusCode());
                return "票据识别失败：HTTP " + response.getStatusCode().value();
            }

        } catch (Exception e) {
            log.error("票据识别请求异常: fileId={}", fileId, e);
            return "票据识别失败：" + e.getMessage();
        }
    }

    private String getFileEnd(String fileType) {
        if (fileType == null || fileType.isEmpty()) {
            return null;
        }

        return switch (fileType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "application/ofd" -> "ofd";
            case "application/pdf" -> "pdf";
            default -> null;
        };
    }

    /**
     * 格式化API识别响应结果
     */
    private String formatResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            int code = root.path("code").asInt(-1);
            String msg = root.path("msg").asText("");

            if (code != 0) {
                return "票据识别失败 [" + code + "]：" + msg;
            }

            JsonNode data = root.path("data");
            if (data.isMissingNode() || data.isNull()) {
                return "票据识别成功，但未返回数据";
            }

            return data.toString();

        } catch (Exception e) {
            log.warn("格式化响应异常，返回原始JSON", e);
            return rawJson;
        }
    }

    /**
     * 格式化API查验响应结果
     */
    private String formatVerifierResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            int code = root.path("code").asInt(-1);
            String msg = root.path("msg").asText("");

            if (code != 0) {
                return "票据查验失败 [" + code + "]：" + msg;
            }

            JsonNode data = root.path("data");
            if (data.isMissingNode() || data.isNull()) {
                return "票据查验成功，但未返回数据";
            }

            return data.toString();

        } catch (Exception e) {
            log.warn("格式化查验响应异常，返回原始JSON", e);
            return rawJson;
        }
    }

    /**
     * 构建鉴权请求头（clientid、timestamp、token）
     */
    private HttpHeaders buildAuthHeaders() {
        long timestamp = System.currentTimeMillis();
        String token = dzintAuthTokenUtil.generateToken(timestamp);
        String clientId = dzintAuthTokenUtil.getClientId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("clientid", clientId);
        headers.set("timestamp", String.valueOf(timestamp));
        headers.set("token", token);
        return headers;
    }
}
