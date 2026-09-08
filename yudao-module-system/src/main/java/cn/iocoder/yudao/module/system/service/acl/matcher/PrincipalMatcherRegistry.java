package cn.iocoder.yudao.module.system.service.acl.matcher;

import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 授权主体匹配策略注册表
 * <p>
 * 汇总所有 {@link PrincipalMatcher}，按 {@link PrincipalType} 提供统一匹配入口。
 *
 * @author IIMS
 */
@Component
public class PrincipalMatcherRegistry {

    private final Map<PrincipalType, PrincipalMatcher> matchers;

    public PrincipalMatcherRegistry(List<PrincipalMatcher> matchers) {
        this.matchers = matchers.stream()
                .collect(Collectors.toMap(PrincipalMatcher::type, Function.identity()));
    }

    /**
     * 判断当前主体是否命中指定类型的授权主体编号
     */
    public boolean matches(PrincipalType type, AclPrincipal principal, Long principalId) {
        PrincipalMatcher matcher = matchers.get(type);
        return matcher != null && matcher.matches(principal, principalId);
    }
}
