package cn.iocoder.yudao.module.infra.service.file;

/**
 * 用户文件访问权限扩展点
 * <p>
 * infra 模块不依赖具体业务模块，各业务模块可通过实现该接口，
 * 为 {@code /infra/file/user/get} 提供额外的文件访问授权判断。
 *
 * @author IIMS
 */
public interface FileAccessChecker {

    /**
     * 判断当前用户是否可以访问指定文件
     *
     * @param fileId 文件 ID
     * @return true=允许访问
     */
    boolean canAccess(Long fileId);

}
