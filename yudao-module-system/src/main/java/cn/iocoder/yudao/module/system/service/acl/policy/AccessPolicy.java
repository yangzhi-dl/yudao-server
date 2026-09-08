package cn.iocoder.yudao.module.system.service.acl.policy;

import cn.iocoder.yudao.module.system.service.acl.model.AccessContext;
import cn.iocoder.yudao.module.system.service.acl.model.AccessDecision;

/**
 * ACL 访问决策策略（责任链的一环）
 * <p>
 * 每个策略只负责判断自己该不该放行；不归它管时返回 {@link AccessDecision#ABSTAIN}，
 * 由决策引擎按 {@link #order()} 顺序继续执行下一个策略。
 *
 * @author IIMS
 */
public interface AccessPolicy {

    /**
     * 策略执行顺序，值越小越先执行
     */
    int order();

    /**
     * 对当前访问上下文做出决策
     */
    AccessDecision decide(AccessContext context);

}
