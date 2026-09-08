package cn.iocoder.yudao.module.system.service.acl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionGrantReqVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionRespVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionRevokeReqVO;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * ACL 权限管理服务
 * <p>
 * 面向管理后台提供授权、撤销、设置、分页查询及可用主体类型等管理能力。
 * 访问判定（当前用户能否读取资源）统一由 {@link cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine} 负责。
 *
 * @author IIMS
 */
public interface AclPermissionService {

    /**
     * 授予权限（支持 USER / ORGANIZATION / POST / ROLE）
     */
    void grantPermission(AclPermissionGrantReqVO dto);

    /**
     * 撤销权限
     */
    void revokePermission(AclPermissionRevokeReqVO dto);

    /**
     * 设置权限（覆盖已有权限，非叠加）
     */
    void setPermission(AclPermissionGrantReqVO dto);

    /**
     * 分页获取资源的授权记录（含解析后的主体名称）
     */
    PageResult<AclPermissionRespVO> pagePermissions(AclPermissionPageReqVO dto);

    /**
     * 获取当前用户可用的授权主体类型集合
     * <p>
     * 依据当前用户的角色（多角色取并集）与租户套餐（取交集）计算；
     * 系统租户无套餐限制，仅按角色过滤。
     *
     * @return 可用授权主体集合
     */
    Set<PrincipalType> getAvailablePrincipalTypes();

    /**
     * 获取当前用户对指定资源的「父级 ACL 授权」过期时间
     * <p>
     * 父级即授权人赖以授权的 AUTH 来源链路节点（与授权动作 {@link #grantPermission} 口径一致）。
     * 子级授权（继承父级）的过期时间不得超过该值，用于前端实现过期时间区间授权
     * （可选项为 [当前时间, 父级过期时间]）。
     *
     * @param resourceType 资源类型（大写枚举名，如 FILE / WIKI / DOCUMENT）
     * @param resourceId   资源ID
     * @return 父级过期时间；null 表示无父级限制（根授权 / 超管 / 父级已过期）
     */
    LocalDateTime getParentExpireTime(String resourceType, Long resourceId);
}
