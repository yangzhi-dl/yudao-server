package cn.iocoder.yudao.module.ai.common.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface PdfToImageService {

    List<BufferedImage> pdfToImages(File pdfFile) throws IOException;

    BufferedImage pdfToImage(File pdfFile, int pageNumber) throws IOException;

}
