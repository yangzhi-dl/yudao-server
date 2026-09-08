package cn.iocoder.yudao.module.system.service.acl.matcher;

import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;

/**
 * 授权主体匹配策略
 * <p>
 * 每种 {@link PrincipalType} 对应一个实现，负责判断当前主体是否命中某个授权主体编号。
 * 通过策略模式消除原先散落在 Service 中的 switch 分派。
 *
 * @author IIMS
 */
public interface PrincipalMatcher {

    /**
     * 当前策略支持的授权主体类型
     */
    PrincipalType type();

    /**
     * 判断当前主体是否命中指定授权主体编号
     *
     * @param principal   当前访问主体
     * @param principalId 授权记录中的主体编号
     * @return true=命中
     */
    boolean matches(AclPrincipal principal, Long principalId);

}
