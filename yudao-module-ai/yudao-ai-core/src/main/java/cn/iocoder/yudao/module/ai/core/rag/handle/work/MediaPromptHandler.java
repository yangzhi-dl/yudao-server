package cn.iocoder.yudao.module.ai.core.rag.handle.work;


import cn.iocoder.yudao.module.ai.common.model.entity.DocumentResponse;
import cn.iocoder.yudao.module.ai.common.service.MarkdownConversionService;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelUseInfo;
import cn.iocoder.yudao.module.ai.core.rag.aspect.FileTypeHandler;
import cn.iocoder.yudao.module.ai.core.rag.enums.FileModelTypeEnum;
import cn.iocoder.yudao.module.ai.core.rag.handle.PromptHandler;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@FileTypeHandler({"jpg", "jpeg", "png"})
public class MediaPromptHandler implements PromptHandler {

    private final FileService fileService;

    private final MarkdownConversionService markdownConversionService;

    public MediaPromptHandler(FileService fileService, MarkdownConversionService markdownConversionService) {
        this.fileService = fileService;
        this.markdownConversionService = markdownConversionService;
    }

    @Override
    public ModelUseInfo handle(FileDO file) {
        List<String> context = new ArrayList<>();
        try {
            byte[] fileContent = fileService.getFileContent(file.getConfigId(), file.getPath());
            String base64 = Base64.getEncoder().encodeToString(fileContent);
            DocumentResponse documentResponse = markdownConversionService.convertImageToMarkdownSync(base64);
            context.add(documentResponse.getMarkdown());
        } catch (Exception e) {
            log.error("图片转markdown出错{}", e.getMessage());
        }
        return ModelUseInfo.builder().type(FileModelTypeEnum.IMAGE).context(context).build();
    }
}