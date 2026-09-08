package cn.iocoder.yudao.module.bpm.dal.mysql.acl;

import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.acl.BpmAclPermissionApprovalDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACL 授权审批单据 Mapper
 *
 * @author IIMS
 */
@Mapper
public interface BpmAclPermissionApprovalMapper extends BaseMapperX<BpmAclPermissionApprovalDO> {

    default PageResult<BpmAclPermissionApprovalDO> selectPage(Long userId, BpmAclPermissionApprovalPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BpmAclPermissionApprovalDO>()
                .eqIfPresent(BpmAclPermissionApprovalDO::getUserId, userId)
                .eqIfPresent(BpmAclPermissionApprovalDO::getStatus, reqVO.getStatus())
                .eqIfPresent(BpmAclPermissionApprovalDO::getOperationType, reqVO.getOperationType())
                .eqIfPresent(BpmAclPermissionApprovalDO::getResourceType, reqVO.getResourceType())
                .eqIfPresent(BpmAclPermissionApprovalDO::getResourceId, reqVO.getResourceId())
                .betweenIfPresent(BpmAclPermissionApprovalDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BpmAclPermissionApprovalDO::getId));
    }

}