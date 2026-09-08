package cn.iocoder.yudao.module.ai.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    /**
     * 创建Token（默认30分钟过期）
     */
    public String createToken(Map<String, String> claims, String secret) {
        Date expiresAt = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        return createToken(claims, secret, expiresAt, null);
    }
    
    /**
     * 创建Token（指定过期时间）
     */
    public String createToken(Map<String, String> claims, String secret, Date expiresDate) {
        return createToken(claims, secret, expiresDate, null);
    }
    
    /**
     * 创建Token（指定过期时间和签发者）
     * 这是您需要的方法
     */
    public String createToken(Map<String, String> claims, String secret, Date expiresDate, String issuer) {
        return createToken(claims, secret, new Date(), expiresDate, issuer);
    }
    
    /**
     * 完整参数的创建Token方法
     */
    public String createToken(Map<String, String> claims, String secret, Date issuedAt, Date expiresAt, String issuer) {
        String token = null;
        
        try {
            JWTCreator.Builder builder = JWT.create();
            
            // 设置Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            builder.withHeader(header);
            
            // 设置发布时间和过期时间
            builder.withIssuedAt(issuedAt);
            builder.withExpiresAt(expiresAt);
            
            // 设置签发者
            if (issuer != null && !issuer.trim().isEmpty()) {
                builder.withIssuer(issuer);
            }
            
            // 设置自定义claims
            if (claims != null && !claims.isEmpty()) {
                for (Map.Entry<String, String> entry : claims.entrySet()) {
                    builder.withClaim(entry.getKey(), entry.getValue());
                }
            }
            
            // 签名并生成token
            Algorithm algorithm = Algorithm.HMAC256(secret);
            token = builder.sign(algorithm);
            
            log.debug("JWT创建成功，过期时间：{}", expiresAt);
            
        } catch (Exception e) {
            log.error("JWT创建失败", e);
            throw new RuntimeException("JWT创建失败: " + e.getMessage(), e);
        }
        
        return token;
    }
    
    /**
     * 验证并解码Token
     */
    public DecodedJWT decodeAndVerify(String token, String secret, String issuer) {
        try {
            Verification verification = JWT.require(Algorithm.HMAC256(secret));
            
            if (issuer != null && !issuer.trim().isEmpty()) {
                verification.withIssuer(issuer);
            }
            
            JWTVerifier verifier = verification.build();
            return verifier.verify(token);
            
        } catch (Exception e) {
            log.error("JWT验证失败：{}", token, e);
            return null;
        }
    }
    
    /**
     * 仅解码Token（不验证签名）
     */
    public DecodedJWT decode(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            log.error("JWT解析失败：{}", token, e);
            return null;
        }
    }
    
    /**
     * 获取Token中的JSON数据（验证后）
     */
    public JSONObject getJwtFromToken(String token, String secret, String issuer) {
        DecodedJWT jwt = decodeAndVerify(token, secret, issuer);
        return toJsonJwt(jwt);
    }
    
    /**
     * 获取Token中的JSON数据（仅解码）
     */
    public JSONObject getJwtFromToken(String token) {
        DecodedJWT jwt = decode(token);
        return toJsonJwt(jwt);
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean checkExp(JSONObject jwt, long currTime) {
        if (jwt != null && jwt.containsKey("exp")) {
            return jwt.getLong("exp") >= currTime;
        }
        return false;
    }
    
    public boolean checkExp(JSONObject jwt) {
        return checkExp(jwt, System.currentTimeMillis());
    }
    
    /**
     * 将DecodedJWT转换为JSONObject
     */
    private JSONObject toJsonJwt(DecodedJWT jwt) {
        if (jwt == null || jwt.getPayload() == null) {
            return null;
        }
        
        try {
            String payload = new String(Base64.getDecoder().decode(jwt.getPayload()), StandardCharsets.UTF_8);
            return JSON.parseObject(payload);
        } catch (Exception e) {
            log.error("JWT payload解析失败", e);
            return null;
        }
    }
    
    /**
     * Base64解码字符串
     */
    private String decodeStr(String str) {
        return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
    }
}