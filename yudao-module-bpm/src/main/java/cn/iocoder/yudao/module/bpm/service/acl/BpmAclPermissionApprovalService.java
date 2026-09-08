package cn.iocoder.yudao.module.bpm.service.acl;

import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.acl.BpmAclPermissionApprovalDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * ACL 授权审批 Service
 * <p>
 * 将 ACL 的直接授权/撤销/设置动作包装为 OA 审批单据：创建单据并发起流程，
 * 审批通过后由监听器调用真正的授权逻辑。
 *
 * @author IIMS
 */
public interface BpmAclPermissionApprovalService {

    /**
     * 创建授权审批单据并发起流程
     *
     * @param userId      申请人用户编号
     * @param createReqVO 创建信息
     * @return 单据编号
     */
    Long createApproval(Long userId, BpmAclPermissionApprovalCreateReqVO createReqVO);

    /**
     * 获得授权审批单据
     *
     * @param id 单据编号
     * @return 单据
     */
    BpmAclPermissionApprovalDO getApproval(Long id);

    /**
     * 获得授权审批单据分页
     *
     * @param userId  申请人用户编号
     * @param pageReqVO 分页条件
     * @return 单据分页
     */
    PageResult<BpmAclPermissionApprovalDO> getApprovalPage(Long userId, BpmAclPermissionApprovalPageReqVO pageReqVO);

    /**
     * 审批通过后，按单据内容执行真实的 ACL 授权/撤销/设置
     *
     * @param approval 单据（需为审批通过态）
     */
    void applyPermissions(BpmAclPermissionApprovalDO approval);

    /**
     * 更新单据审批状态
     *
     * @param id     单据编号
     * @param status 审批状态，参见 {@link cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum}
     */
    void updateApprovalStatus(Long id, Integer status);

}