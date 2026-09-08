package cn.iocoder.yudao.module.ai.knowledge.grap.service.impl;

import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.module.ai.knowledge.grap.service.WikiGraphService;
import cn.iocoder.yudao.module.ai.knowledge.grap.model.GraphOperation;
import cn.iocoder.yudao.module.ai.knowledge.grap.model.GraphRoute;
import cn.iocoder.yudao.module.ai.knowledge.grap.model.GraphWidget;
import cn.iocoder.yudao.module.ai.knowledge.grap.properties.WikiGraphProperties;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import cn.iocoder.yudao.module.ai.common.model.entity.AlgorithmResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import cn.iocoder.yudao.module.ai.common.utils.JwtUtil;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WikiGraphServiceImpl implements WikiGraphService {

    private final JwtUtil jwtUtil;
    private final SecurityFrameworkService securityFrameworkService;

    // 缓存常用配置，避免重复长链访问
    private final WikiGraphProperties.Config config;
    private final WikiGraphProperties.RestClient restClient;
    private final WikiGraphProperties.Service service;
    private final WikiGraphProperties.Config.InterfaceParam interfaceParam;

    public WikiGraphServiceImpl(WikiGraphProperties properties, JwtUtil jwtUtil,
                                SecurityFrameworkService securityFrameworkService) {
        this.jwtUtil = jwtUtil;
        this.securityFrameworkService = securityFrameworkService;
        this.config = properties.getConfig();
        this.restClient = properties.getRestClient();
        this.service = properties.getService();
        this.interfaceParam = config.getInterfaceParam();
    }

    @Override
    public Boolean uploadWikiDocument(Long wikiId, Long documentId, File file) {
        Integer connectTimeout = restClient.getConnectTimeout();
        Integer responseTimeout = restClient.getResponseTimeout();
        String token = config.getToken();
        String baseUrl = config.getBaseUrl();
        String prefix = config.getPrefix();
        String uri = interfaceParam.getUpload().getUri();
        String mode = interfaceParam.getUpload().getMode();

        log.info("开始构建知识图谱，wikiId: {}, documentId: {}, mode: {}",
                wikiId, documentId, mode);

        String kmsid = String.valueOf(wikiId);
        String docid = String.valueOf(documentId);

        try {
            HttpClient httpClient = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                    .responseTimeout(Duration.ofMillis(responseTimeout));

            WebClient webClient = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                    .build();

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getName();
                }
            };

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", fileResource);

            AlgorithmResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("kmsid", prefix + kmsid)
                            .queryParam("docid", docid)
                            .build())
                    .header("X-API-Key", token)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(formData)
                    .retrieve().bodyToMono(AlgorithmResponse.class)
                    .block(Duration.ofMillis(responseTimeout + connectTimeout));

            if (response != null) {
                Integer code = response.getCode();
                if (code == 200 || code == 201) {
                    return true;
                }
                log.error("知识图谱构建失败，code: {}, message: {}", code, response.getMessage());
                return false;
            }
            return false;

        } catch (WebClientResponseException e) {
            log.error("知识图谱服务 HTTP 请求异常", e);
            return false;
        } catch (Exception e) {
            log.error("构建知识图谱异常", e);
            return false;
        }
    }

    @Override
    public String getPermissionsToken() {
        Long expire = service.getExpire();
        String secret = service.getSecret();
        String issuer = service.getIssuer();
        Date expiration = new Date(new Date().getTime() + expire);
        Map<String, String> data = new HashMap<>();

        data.put("route", JSONObject.toJSONString(new GraphRoute(
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:preview"),
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:operate"))));
        GraphOperation node = new GraphOperation(
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:create"),
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:update"),
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:delete"));
        GraphOperation relationship = new GraphOperation(
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:create"),
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:update"),
                securityFrameworkService.hasAnyPermissions("ai:wiki:graph:delete"));
        data.put("widget", JSONObject.toJSONString(new GraphWidget(node, relationship)));
        return jwtUtil.createToken(data, secret, expiration, issuer);
    }

    @Override
    public Boolean deleteWikiDocument(Long wikiId, Long documentId) {
        Integer connectTimeout = restClient.getConnectTimeout();
        Integer responseTimeout = restClient.getResponseTimeout();
        String token = config.getToken();
        String baseUrl = config.getBaseUrl();
        String prefix = config.getPrefix();
        String uri = interfaceParam.getDeleteDocument().getUri();

        String kmsid = prefix + wikiId;
        String docid = String.valueOf(documentId);

        log.info("开始删除知识库的文档图谱，wikiId: {}, documentId: {}, kmsid: {}, docid: {}",
                wikiId, documentId, kmsid, docid);

        try {
            HttpClient httpClient = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                    .responseTimeout(Duration.ofMillis(responseTimeout));

            WebClient webClient = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl(baseUrl)
                    .build();

            AlgorithmResponse response = webClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri + "/{kmsid}")
                            .queryParam("docid", docid)
                            .build(kmsid))
                    .header("X-API-Key", token)
                    .retrieve()
                    .bodyToMono(AlgorithmResponse.class)
                    .block(Duration.ofMillis(responseTimeout + connectTimeout));

            if (response != null) {
                Integer code = response.getCode();
                if (code == 200) {
                    log.info("知识库的文档图谱删除成功，wikiId: {}, documentId: {}", wikiId, documentId);
                    return true;
                }
                log.error("知识库的文档图谱删除失败，code: {}, message: {}", code, response.getMessage());
                return false;
            }
            return false;

        } catch (WebClientResponseException e) {
            log.error("知识图谱服务 HTTP 请求异常，状态码: {}, 响应体: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        } catch (Exception e) {
            log.error("删除知识库的文档图谱异常，wikiId: {}, documentId: {}", wikiId, documentId, e);
            return false;
        }
    }

    @Override
    public Boolean deleteWiki(Long wikiId) {
        Integer connectTimeout = restClient.getConnectTimeout();
        Integer responseTimeout = restClient.getResponseTimeout();
        String token = config.getToken();
        String baseUrl = config.getBaseUrl();
        String prefix = config.getPrefix();
        String uri = interfaceParam.getDeleteWiki().getUri();

        String kmsid = prefix + wikiId;

        log.info("开始删除整个知识库的图谱，wikiId: {}, kmsid: {}", wikiId, kmsid);

        try {
            HttpClient httpClient = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                    .responseTimeout(Duration.ofMillis(responseTimeout));

            WebClient webClient = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl(baseUrl)
                    .build();

            AlgorithmResponse response = webClient.delete()
                    .uri(uri + "/{kmsid}", kmsid)
                    .header("X-API-Key", token)
                    .retrieve()
                    .bodyToMono(AlgorithmResponse.class)
                    .block(Duration.ofMillis(responseTimeout + connectTimeout));

            if (response != null) {
                Integer code = response.getCode();
                if (code == 200) {
                    log.info("整个知识库的图谱删除成功，wikiId: {}", wikiId);
                    return true;
                }
                log.error("知识库的图谱删除失败，code: {}, message: {}", code, response.getMessage());
                return false;
            }
            return false;

        } catch (WebClientResponseException e) {
            log.error("知识图谱服务 HTTP 请求异常，状态码: {}, 响应体: {}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        } catch (Exception e) {
            log.error("删除整个知识库的图谱异常，wikiId: {}", wikiId, e);
            return false;
        }
    }
}