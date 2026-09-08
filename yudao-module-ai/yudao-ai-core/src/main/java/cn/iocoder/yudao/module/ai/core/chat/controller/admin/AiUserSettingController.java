package cn.iocoder.yudao.module.ai.core.chat.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelSettingDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SaveModelSettingDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AiUserSettingPageVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AssignModelResultVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatSettingService;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.USER_SETTING_USER_ID_NOT_NULL;

/**
 * 用户 AI 模型设置管理
 */
@Slf4j
@RestController
@RequestMapping("/ai/user-setting")
@Validated
public class AiUserSettingController {

    private final AiChatSettingService aiChatSettingService;
    private final AdminUserService adminUserService;
    private final DeptService deptService;
    private final FileService fileService;

    public AiUserSettingController(AiChatSettingService aiChatSettingService,
                                   AdminUserService adminUserService,
                                   DeptService deptService,
                                   FileService fileService) {
        this.aiChatSettingService = aiChatSettingService;
        this.adminUserService = adminUserService;
        this.deptService = deptService;
        this.fileService = fileService;
    }

    /**
     * 分页查询用户及配置状态
     */
    @GetMapping("/page")
    @Operation(summary = "获得 AI 用户配置状态分页")
    public CommonResult<PageResult<AiUserSettingPageVO>> page(@Valid UserPageReqVO reqVO) {
        PageResult<AdminUserDO> users = adminUserService.getUserPage(reqVO);
        List<Long> userIds = users.getList().stream().map(AdminUserDO::getId).toList();
        Set<Long> configuredIds = new HashSet<>(aiChatSettingService.selectConfiguredUserIds(userIds));
        // 组织信息映射
        Map<Long, DeptDO> deptMap = deptService.getDeptMap(
                convertList(users.getList(), AdminUserDO::getDeptId));
        List<AiUserSettingPageVO> result = users.getList().stream().map(user -> {
            AiUserSettingPageVO vo = new AiUserSettingPageVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            // 头像文件 ID -> 真实图片 URL（由后端处理）
            vo.setAvatar(resolveAvatarUrl(user.getAvatar()));
            vo.setMobile(user.getMobile());
            DeptDO dept = deptMap.get(user.getDeptId());
            vo.setDeptName(dept != null ? dept.getName() : null);
            vo.setConfigured(configuredIds.contains(user.getId()));
            return vo;
        }).toList();
        return success(new PageResult<>(result, users.getTotal()));
    }

    /**
     * 将头像文件 ID 转换为真实可访问的图片 URL
     */
    private String resolveAvatarUrl(String avatar) {
        if (StringUtils.isBlank(avatar)) {
            return null;
        }
        try {
            Long fileId = Long.valueOf(avatar);
            return fileService.presignGetUrl(fileId, 600);
        } catch (NumberFormatException e) {
            // 非数字 ID，原样返回（可能已是普通 URL）
            return avatar;
        }
    }

    /**
     * 查询指定用户的模型设置；无配置时自动初始化默认配置
     */
    @GetMapping("/get")
    @Operation(summary = "获得用户 AI 模型设置")
    public CommonResult<ModelSettingDO> get(@RequestParam("userId") Long userId) {
        ModelSettingDO setting = aiChatSettingService.getUserModelSetting(String.valueOf(userId));
        if (setting == null) {
            aiChatSettingService.initUserModelSetting(userId);
            setting = aiChatSettingService.getUserModelSetting(String.valueOf(userId));
        }
        return success(setting);
    }

    /**
     * 保存指定用户的模型设置
     */
    @PostMapping("/save")
    @Operation(summary = "保存用户 AI 模型设置")
    public CommonResult<Boolean> save(@RequestBody @Valid SaveModelSettingDTO dto) {
        if (dto.getUserId() == null) {
            throw exception(USER_SETTING_USER_ID_NOT_NULL);
        }
        aiChatSettingService.saveUserModelSetting(dto.getUserId(), dto.getConfigs());
        return success(true);
    }

    /**
     * 删除指定用户的模型设置
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除用户 AI 模型设置")
    public CommonResult<Boolean> delete(@RequestParam("userId") Long userId) {
        aiChatSettingService.delete(String.valueOf(userId));
        return success(true);
    }

    /**
     * 模型为中心：将模型设置为指定用户其「对应类型」的默认模型
     */
    @PostMapping("/assign")
    @Operation(summary = "模型设置为用户对应类型的默认模型")
    public CommonResult<AssignModelResultVO> assign(@RequestParam("userId") Long userId,
                                                    @RequestParam("modelId") Long modelId,
                                                    @RequestParam(value = "force", required = false)
                                                    Boolean force) {
        if (userId == null) {
            throw exception(USER_SETTING_USER_ID_NOT_NULL);
        }
        return success(aiChatSettingService.assignModelToUser(userId, modelId, force));
    }
}
