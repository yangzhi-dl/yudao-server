package cn.iocoder.yudao.module.hub.core.portal.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Hub 门户错误码。
 *
 * Hub 模块使用 1-070-000-000 段。
 */
public interface ErrorCodeConstants {

    ErrorCode PORTAL_CONTENT_NOT_EXISTS = new ErrorCode(1_070_000_000, "门户内容不存在");
    ErrorCode PORTAL_CONTENT_CODE_DUPLICATE = new ErrorCode(1_070_000_001,
            "页面【{}】的内容类型【{}】已存在编码【{}】");

}
