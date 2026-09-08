package cn.iocoder.yudao.module.system.service.acl.metadata;

import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 资源归属元数据注册表
 * <p>
 * 汇总所有 {@link ResourceMetadataProvider}，按 {@link ResourceType} 提供统一查询入口。
 *
 * @author IIMS
 */
@Component
public class ResourceMetadataRegistry {

    private final Map<ResourceType, ResourceMetadataProvider> providers;

    public ResourceMetadataRegistry(List<ResourceMetadataProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(ResourceMetadataProvider::resourceType, Function.identity()));
    }

    /**
     * 获取指定资源类型的元数据 Provider
     */
    public ResourceMetadataProvider get(ResourceType resourceType) {
        ResourceMetadataProvider provider = providers.get(resourceType);
        if (provider == null) {
            throw new IllegalStateException("未注册 ResourceMetadataProvider: " + resourceType);
        }
        return provider;
    }
}
