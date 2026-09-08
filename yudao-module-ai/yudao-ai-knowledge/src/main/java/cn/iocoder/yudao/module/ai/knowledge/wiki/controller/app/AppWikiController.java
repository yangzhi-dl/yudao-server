package cn.iocoder.yudao.module.ai.knowledge.wiki.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.common.model.dto.BasePageListDTO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/wiki")
@Slf4j
public class AppWikiController {

    private final WikiService wikiService;

    public AppWikiController(WikiService wikiService) {
        this.wikiService = wikiService;
    }

    @PostMapping("/page/usage")
    public CommonResult<PageResult<UsageRecordWikiVO>> getUsageRecordWiki(@RequestBody BasePageListDTO dto) {
        return success(wikiService.getUsageRecordWikiPage(dto));
    }

    @PostMapping("/publish/list")
    public CommonResult<PageResult<FindUserWikiPageListVO>> findPublishWikiPageList(@RequestBody @Validated FindAccessibleWikiPageListDTO dto) {
        return success(wikiService.findWikiPermissionPageList(dto));
    }

    @PostMapping("/hot/{limit}")
    public CommonResult<List<HotWikiVO>> getHotWiki(@PathVariable("limit") Integer limit) {
        return success(wikiService.getHotWiki(limit));
    }

}
