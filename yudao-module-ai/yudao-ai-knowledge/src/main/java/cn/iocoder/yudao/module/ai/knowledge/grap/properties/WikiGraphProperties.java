package cn.iocoder.yudao.module.ai.knowledge.grap.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "iims.graph")
public class WikiGraphProperties {

    private RestClient restClient = new RestClient();
    private Config config = new Config();
    private Service service = new Service();

    @Data
    public static class RestClient {
        private Integer connectTimeout = 3600;
        private Integer responseTimeout = 3600;
    }

    @Data
    public static class Config {
        private String baseUrl = "http://localhost:9621";
        private String token = "";
        private String prefix = "kms";
        private InterfaceParam interfaceParam = new InterfaceParam();

        @Data
        public static class InterfaceParam {
            private Upload upload = new Upload();
            private DeleteDocument deleteDocument = new DeleteDocument();
            private DeleteWiki deleteWiki = new DeleteWiki();
        }

        @Data
        public static class Upload {
            private String uri = "/upload";
            private String mode = "hybrid";
        }

        @Data
        public static class DeleteDocument {
            private String uri = "/delete_file";
        }

        @Data
        public static class DeleteWiki {
            private String uri = "/delete";
        }
    }

    @Data
    public static class Service {
        private String issuer = "dzint";
        private String secret = "dzint1688";
        private Long expire = 21600000L;
    }
}