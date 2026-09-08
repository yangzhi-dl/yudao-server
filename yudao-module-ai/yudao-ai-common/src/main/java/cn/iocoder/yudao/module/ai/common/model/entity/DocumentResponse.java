package cn.iocoder.yudao.module.ai.common.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponse {

    private Boolean success;

    private String markdown;

    private List<ImageInfo> files;

    private String message;

    @Data
    public static class ImageInfo {
        private Integer index;
        private String base64;
        private String context;
    }

}
