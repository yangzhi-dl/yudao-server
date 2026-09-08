package cn.iocoder.yudao.module.bpm.service.acl.listener;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.bpm.dal.dataobject.acl.BpmAclPermissionApprovalDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.acl.BpmAclPermissionApprovalMapper;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.acl.BpmAclPermissionApprovalService;
import cn.iocoder.yudao.module.bpm.service.acl.BpmAclPermissionApprovalServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Objects;

/**
 * ACL 授权审批的结果监听器
 * <p>
 * 审批【通过】时，以申请人的身份执行真正的 ACL 授权/撤销/设置；
 * 其余状态（拒绝、取消）仅更新单据状态。
 *
 * @author IIMS
 */
@Slf4j
@Component
public class BpmAclPermissionApprovalStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private BpmAclPermissionApprovalMapper approvalMapper;
    @Resource
    private BpmAclPermissionApprovalService approvalService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmAclPermissionApprovalServiceImpl.PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        Long approvalId = Long.parseLong(event.getBusinessKey());
        BpmAclPermissionApprovalDO approval = approvalMapper.selectById(approvalId);
        if (approval == null) {
            log.warn("[onEvent][流程({}) 找不到对应的 ACL 授权审批单据({})]", event.getId(), approvalId);
            return;
        }
        // 1. 审批通过：以申请人的身份执行真正的授权/撤销/设置
        if (Objects.equals(event.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
            executeWithApplicantContext(approval);
        }
        // 2. 更新单据状态
        approvalService.updateApprovalStatus(approvalId, event.getStatus());
    }

    /**
     * 在申请人（单据 owner）的登录上下文下执行授权
     * <p>
     * 监听器运行在流程回调线程中，无当前登录用户；而授权服务通过
     * {@link SecurityFrameworkUtils#getLoginUserId()} 校验发起人身份并记录 grantBy，
     * 故需临时注入申请人的安全上下文，执行完必须清理避免污染线程。
     */
    private void executeWithApplicantContext(BpmAclPermissionApprovalDO approval) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(approval.getUserId());
        loginUser.setUserType(UserTypeEnum.ADMIN.getValue());
        loginUser.setTenantId(approval.getTenantId());
        loginUser.setVisitTenantId(approval.getTenantId());
        try {
            // 回调线程无 HttpServletRequest，直接注入 Authentication（避免 setLoginUser 对 request 判空的 NPE）
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
            approvalService.applyPermissions(approval);
        } catch (Exception ex) {
            log.error("[executeWithApplicantContext][单据({}) 审批通过后执行授权异常]", approval.getId(), ex);
            throw ex;
        } finally {
            // 清理，避免登录信息泄漏到工作流回调线程
            SecurityContextHolder.clearContext();
        }
    }

}