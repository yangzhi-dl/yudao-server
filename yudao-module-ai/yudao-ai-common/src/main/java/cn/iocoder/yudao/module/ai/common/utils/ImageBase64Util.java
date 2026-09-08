package cn.iocoder.yudao.module.ai.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
public class ImageBase64Util {
    
    private static final String IMAGE_FORMAT = "png";
    
    /**
     * 将BufferedImage转换为Base64字符串（不存储到内存，直接返回）
     */
    public static String imageToBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, IMAGE_FORMAT, baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }
    
    /**
     * 验证Base64字符串
     */
    public static boolean isValidBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}