package cn.iocoder.yudao.module.ai.common.service.impl;

import cn.iocoder.yudao.module.ai.common.service.PdfToImageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;  // ← 新增导入
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PdfToImageServiceImpl implements PdfToImageService {

    private static final int DPI = 200;

    @Override
    public List<BufferedImage> pdfToImages(File pdfFile) throws IOException {
        List<BufferedImage> images = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            log.debug("PDF页数: {}", pageCount);

            for (int page = 0; page < pageCount; page++) {
                try {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(page, DPI);
                    images.add(image);
                    log.debug("第 {} 页渲染完成", page + 1);
                } catch (IOException e) {
                    log.error("渲染第 {} 页失败", page + 1, e);
                    throw e;
                }
            }
        }
        return images;
    }

    @Override
    public BufferedImage pdfToImage(File pdfFile, int pageNumber) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            return pdfRenderer.renderImageWithDPI(pageNumber - 1, DPI);
        }
    }
}