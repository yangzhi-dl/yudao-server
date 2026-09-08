package cn.iocoder.yudao.module.ai.common.service;

import cn.iocoder.yudao.module.ai.common.model.entity.DocumentResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface MarkdownConversionService {

    void startConsumer();

    DocumentResponse convertImageToMarkdownSync(String base64Image) throws IOException;

    List<DocumentResponse> convertPdfToMarkdownSync(File pdfFile) throws IOException;

}
