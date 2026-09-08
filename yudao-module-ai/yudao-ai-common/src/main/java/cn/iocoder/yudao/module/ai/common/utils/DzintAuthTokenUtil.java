package cn.iocoder.yudao.module.ai.common.utils;

import cn.iocoder.yudao.module.ai.common.properties.DzintAuthProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * DZINT鉴权参数生成工具类
 *
 */
@Component
public class DzintAuthTokenUtil {

    private final DzintAuthProperties authProperties;

    public DzintAuthTokenUtil(DzintAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    /**
     * 生成鉴权Token（使用配置中的clientId、secret）
     *
     * @param timestamp 时间戳（毫秒）
     * @return MD5签名字符串
     */
    public String generateToken(long timestamp) {
        DzintAuthProperties.Client client = authProperties.getClient();
        String separator = client.getSeparator();
        String format = String.format("%s%s%s%s%s", client.getId(), separator,
                client.getSecret(), separator, timestamp);
        return md5(format);
    }

    /**
     * 生成鉴权Token（使用当前时间戳和配置中的clientId、secret）
     *
     * @return MD5签名字符串
     */
    public String generateToken() {
        return generateToken(System.currentTimeMillis());
    }

    /**
     * MD5加密
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 获取客户端ID
     */
    public String getClientId() {
        return authProperties.getClient().getId();
    }
}