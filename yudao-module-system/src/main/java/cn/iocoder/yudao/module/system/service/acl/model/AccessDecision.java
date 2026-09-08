package cn.iocoder.yudao.module.system.service.acl.model;

/**
 * 单次访问判定的结果
 * <p>
 * GRANT=放行；DENY=拒绝；ABSTAIN=当前策略不负责，交给责任链中的下一个策略继续处理。
 *
 * @author IIMS
 */
public enum AccessDecision {

    GRANT,
    DENY,
    ABSTAIN

}
