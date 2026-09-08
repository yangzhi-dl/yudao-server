package cn.iocoder.yudao.module.ai.knowledge.wiki.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.FindDocumentDetailDTO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiSettings;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.FindBatchCatalogDocumentVO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.FindWikiCatalogListVO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.FindWikiDocumentDetailVO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.FindWikiPageListVO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/wiki")
public class WikiController {

    private final WikiService wikiService;

    public WikiController(WikiService wikiService) {
        this.wikiService = wikiService;
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addWiki(@RequestBody @Validated AddWikiDTO addWikiDto) {
        wikiService.addWiki(addWikiDto);
        return success(true);
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> deleteWiki(@RequestBody @Validated DeleteWikiDTO deleteWikiDto) {
        wikiService.deleteWiki(deleteWikiDto);
        return success(true);
    }

    @PostMapping("/list")
    public CommonResult<PageResult<FindWikiPageListVO>> findWikiPageList(@RequestBody @Validated FindWikiPageListDTO dto) {
        return success(wikiService.findWikiPageList(dto));
    }

    @GetMapping("/settings/detail/{wikiId}")
    public CommonResult<WikiSettings> getWikiSettings(@PathVariable Long wikiId) {
        return success(wikiService.getWikiSettings(wikiId));
    }

    @PostMapping("/settings/update")
    public CommonResult<Boolean> updateWikiSettings(@RequestBody @Validated UpdateWikiSettingsDTO dto) {
        wikiService.updateWikiSettings(dto);
        return success(true);
    }

    @PostMapping("/isTop/update")
    public CommonResult<Boolean> updateWikiIsTop(@RequestBody @Validated UpdateWikiIsTopDTO updateWikiIsTopDto) {
        wikiService.updateWikiIsTop(updateWikiIsTopDto);
        return success(true);
    }

    @PostMapping("/update")
    public CommonResult<Boolean> updateWiki(@RequestBody @Validated UpdateWikiDTO updateWikiDto) {
        wikiService.updateWiki(updateWikiDto);
        return success(true);
    }

    @PostMapping("/embedding/{wikiId}")
    public CommonResult<Boolean> documentEmbedding(@PathVariable Long wikiId) {
        wikiService.passageEmbedding(wikiId);
        return success(true);
    }

    @PostMapping("/catalog/list")
    public CommonResult<List<FindWikiCatalogListVO>> findWikiCatalogList(@RequestBody @Validated FindWikiCatalogListDTO dto) {
        return success(wikiService.findWikiCatalogList(dto));
    }

    @PostMapping("/catalog/update")
    public CommonResult<Boolean> updateWikiCatalogs(@RequestBody @Valid UpdateWikiCatalogDTO dto) {
        return success(wikiService.updateWikiCatalogs(dto));
    }

    @PostMapping("/catalog/documents")
    public CommonResult<PageResult<FindWikiCatalogListVO>> findCatalogDocuments(@RequestBody @Validated FindWikiCatalogListDTO dto) {
        return success(wikiService.findCatalogDocuments(dto.getWikiId(), dto.getParentId(), dto.getPage(), dto.getPageSize()));
    }

    @PostMapping("/catalog/documents/batch")
    public CommonResult<Map<Long, List<FindBatchCatalogDocumentVO>>> findBatchCatalogDocuments(@RequestBody @Validated BatchFindWikiCatalogDocumentsDTO dto) {
        return success(wikiService.findBatchCatalogDocuments(dto.getWikiId(), dto.getParentIds()));
    }

    @PostMapping("/catalog/unarchived-documents")
    public CommonResult<PageResult<FindWikiCatalogListVO>> findUnarchivedDocuments(@RequestBody FindWikiCatalogListDTO dto) {
        return success(wikiService.findUnarchivedDocuments(
                dto.getWikiId(), dto.getTitle(), dto.getCategoryId(), dto.getTagIds(), dto.getPage(), dto.getPageSize()));
    }

    @PostMapping("/catalog/archive")
    public CommonResult<Boolean> archiveDocuments(@RequestBody @Validated ArchiveWikiDocumentDTO dto) {
        wikiService.archiveDocuments(dto);
        return success(true);
    }

    @PostMapping("/catalog/remove-document")
    public CommonResult<Boolean> removeDocumentFromCatalog(@RequestBody @Validated RemoveWikiDocumentDTO dto) {
        wikiService.removeDocumentFromCatalog(dto.getWikiId(), dto.getDocumentId());
        return success(true);
    }

    @PostMapping("/document/detail")
    public CommonResult<FindWikiDocumentDetailVO> findDocumentDetail(@RequestBody FindDocumentDetailDTO articleDetailDto) {
        return success(wikiService.findWikiDetail(articleDetailDto));
    }

    @GetMapping("/detail/{wikiId}")
    public CommonResult<FindWikiPageListVO> getWikiDetail(@PathVariable Long wikiId) {
        return success(wikiService.getWikiDetail(wikiId));
    }

    @GetMapping("/graph/{wikiId}")
    public CommonResult<Boolean> generateWikiGraph(@PathVariable Long wikiId) {
        return success(wikiService.generateWikiGraph(wikiId));
    }

    @GetMapping("/graph/token")
    public CommonResult<String> generateWikiGraphToken() {
        return success(wikiService.generateWikiGraphToken());
    }

}
