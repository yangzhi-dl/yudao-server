package cn.iocoder.yudao.module.ai.knowledge.document.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.FindDocumentDetailDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.FindDocumentPageListDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.PublishDocumentDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.SaveDocumentContentDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.SaveDocumentInfoDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.UpdateDocumentDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindDocumentDetailVO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindDocumentInfoDetailVO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindDocumentPageListVO;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/document")
@Slf4j
public class DocumentController {
    
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/create")
    public CommonResult<Boolean> createDocument(@RequestBody @Validated PublishDocumentDTO publishDocumentDto) {
        documentService.createDocument(publishDocumentDto);
        return success(true);
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> deleteDocument(@RequestBody List<Long> ids) {
        documentService.deleteDocument(ids);
        return success(true);
    }

    @PostMapping("/document/conversion")
    public CommonResult<Boolean> documentConversion(@RequestBody List<Long> ids) {
        documentService.documentConversion(ids);
        return success(true);
    }

    @PostMapping("/page")
    public CommonResult<PageResult<FindDocumentPageListVO>> findDocumentPageList(@RequestBody @Validated FindDocumentPageListDTO findDocumentPageListDto) {
        PageResult<FindDocumentPageListVO> articlePageList = documentService.findDocumentPageList(findDocumentPageListDto);
        return success(articlePageList);
    }

    @GetMapping("/detail/{id}")
    public CommonResult<FindDocumentDetailVO> findDocumentDetail(@PathVariable Long id) {
        FindDocumentDetailVO articleDetail = documentService.findDocumentDetail(id);
        return success(articleDetail);
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateDocument(@RequestBody @Validated UpdateDocumentDTO updateDocumentDto) {
        documentService.updateDocument(updateDocumentDto);
        return success(true);
    }

    @PostMapping("/save/content")
    public CommonResult<Boolean> saveDocumentContent(@RequestBody @Validated SaveDocumentContentDTO dto) {
        return success(documentService.saveDocumentContent(dto));
    }

    @PostMapping("/save/info")
    public CommonResult<Boolean> saveDocumentInfo(@RequestBody @Validated SaveDocumentInfoDTO dto) {
        return success(documentService.saveDocumentInfo(dto));
    }

    @PostMapping("/isTop/update/{isTop}")
    public CommonResult<Boolean> updateDocumentIsTop(@PathVariable Boolean isTop, @RequestParam("id") Long id) {
        documentService.updateDocumentIsTop(isTop, id);
        return success(true);
    }

    @PostMapping("/detail/info")
    public CommonResult<FindDocumentInfoDetailVO> findDocumentInfoDetail(@RequestBody FindDocumentDetailDTO dto) {
        return success(documentService.findDocumentInfoDetail(dto));
    }

    @PostMapping("/reload/index")
    public CommonResult<Boolean> reloadDocumentIndex() {
        documentService.reloadDocumentIndex();
        return success(true);
    }

    @PostMapping("/reload/embedding/{id}")
    public CommonResult<Boolean> reloadDocumentEmbedding(@PathVariable Long id) {
        documentService.reloadDocumentEmbedding(id);
        return success(true);
    }

}
