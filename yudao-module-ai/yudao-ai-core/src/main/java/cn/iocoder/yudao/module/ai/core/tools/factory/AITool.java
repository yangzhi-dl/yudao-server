package cn.iocoder.yudao.module.ai.core.tools.factory;

public interface AITool {

    /**
     * 获取工具实例
     */
    Object getToolInstance();

    /**
     * 获取工具名称
     */
    String getName();

    String getTitle();

    String getDescription();

    /**
     * 工具是否启用。默认启用；可通过配置关闭。
     */
    default boolean isEnabled() {
        return true;
    }

}
