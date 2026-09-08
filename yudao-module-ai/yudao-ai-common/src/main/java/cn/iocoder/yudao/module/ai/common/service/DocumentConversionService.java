package cn.iocoder.yudao.module.ai.common.service;

import cn.iocoder.yudao.module.ai.common.model.entity.DocumentTask;
import cn.iocoder.yudao.module.ai.common.model.entity.MarkdownConversionResult;

import java.io.InputStream;
import java.util.List;

public interface DocumentConversionService {

    void convertDocumentImageAsync(DocumentTask task);

    void convertDocumentFileAsync(DocumentTask task);

    List<MarkdownConversionResult> convertDocumentImageSync(InputStream inputStream,
                                                            String fileName) throws Exception;

    List<MarkdownConversionResult> convertDocumentFileSync(InputStream inputStream,
                                                           String fileName,
                                                           Long userId) throws Exception;

}
