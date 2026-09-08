package cn.iocoder.yudao.module.ai.data.governance.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.data.governance.controller.admin.vo.AssetAiCleanReqVO;
import cn.iocoder.yudao.module.ai.data.governance.controller.admin.vo.AssetArchiveReqVO;
import cn.iocoder.yudao.module.ai.data.governance.controller.admin.vo.AssetEditReqVO;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.DataAsset;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.GovernanceRecord;
import cn.iocoder.yudao.module.ai.data.governance.service.DataAssetService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 数据治理 Controller。
 */
@RestController
@RequestMapping("/ai-data/asset")
public class DataGovernanceController {

    private final DataAssetService dataAssetService;

    public DataGovernanceController(DataAssetService dataAssetService) {
        this.dataAssetService = dataAssetService;
    }

    @GetMapping("/get")
    public CommonResult<DataAsset> getAsset(@RequestParam("id") Long id) {
        return success(dataAssetService.getAsset(id));
    }

    @GetMapping("/page")
    public CommonResult<PageResult<DataAsset>> getAssetPage(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return success(dataAssetService.getAssetPage(pageParam, status, title));
    }

    @GetMapping("/content")
    public CommonResult<String> getAssetContent(@RequestParam("id") Long id) {
        return success(dataAssetService.getAssetContent(id));
    }

    @PostMapping("/edit")
    public CommonResult<Boolean> editAsset(@RequestBody @Validated AssetEditReqVO req) {
        dataAssetService.editAsset(req.getAssetId(), req.getTitle(), req.getSummary(), req.getContent());
        return success(true);
    }

    @PostMapping("/ai-clean")
    public CommonResult<Boolean> aiCleanAsset(@RequestBody @Validated AssetAiCleanReqVO req) {
        dataAssetService.aiCleanAsset(req.getAssetId(), req.getPrompt());
        return success(true);
    }

    @PostMapping("/discard")
    public CommonResult<Boolean> discardAsset(@RequestParam("id") Long id) {
        dataAssetService.discardAsset(id);
        return success(true);
    }

    @PostMapping("/archive")
    public CommonResult<Boolean> archiveAsset(@RequestBody @Validated AssetArchiveReqVO req) {
        dataAssetService.archiveAsset(req.getAssetId(), req.getWikiId(), req.getParentId(),
                req.getCategoryId(), req.getTagIds(), req.getTitle(), req.getSummary(),
                req.getCover(), req.getIsTop());
        return success(true);
    }

    @GetMapping("/records")
    public CommonResult<List<GovernanceRecord>> getGovernanceRecords(@RequestParam("id") Long id) {
        return success(dataAssetService.getGovernanceRecords(id));
    }

}
