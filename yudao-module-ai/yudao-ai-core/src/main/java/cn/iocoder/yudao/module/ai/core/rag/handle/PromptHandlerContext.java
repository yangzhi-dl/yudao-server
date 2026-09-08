package cn.iocoder.yudao.module.ai.core.rag.handle;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelUseInfo;
import cn.iocoder.yudao.module.ai.core.rag.aspect.FileTypeHandler;
import cn.iocoder.yudao.module.ai.core.rag.enums.FileModelTypeEnum;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PromptHandlerContext {

    private final Map<String, PromptHandler> handlerMap = new HashMap<>();

    private final ApplicationContext applicationContext;

    public PromptHandlerContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        // 扫描所有实现了 FileHandler 接口的 Bean
        Map<String, PromptHandler> beans = applicationContext.getBeansOfType(PromptHandler.class);
        for (PromptHandler handler : beans.values()) {
            FileTypeHandler annotation = handler.getClass().getAnnotation(FileTypeHandler.class);
            if (annotation != null) {
                for (String fileType : annotation.value()) {
                    handlerMap.put(fileType, handler);
                }
            }
        }
    }

    public ModelUseInfo handleFile(FileDO file) {
        String fileName = file.getName();

        String fileExtension = getFileExtension(fileName);
        PromptHandler handler = handlerMap.get(fileExtension);

        if (handler != null) {
            return handler.handle(file);
        }
        log.warn("No handler found for file type: {}", fileExtension);
        return ModelUseInfo.builder().type(FileModelTypeEnum.TEXT)
                .context(List.of()).build();
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1 || lastIndexOfDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastIndexOfDot + 1).toLowerCase();
    }
}