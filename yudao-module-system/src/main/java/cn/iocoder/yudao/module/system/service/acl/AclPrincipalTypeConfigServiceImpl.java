package cn.iocoder.yudao.module.system.service.acl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPrincipalTypeConfigDO;
import cn.iocoder.yudao.module.system.dal.mysql.acl.AclPrincipalTypeConfigMapper;
import cn.iocoder.yudao.module.system.enums.acl.AclPrincipalTypeOwnerType;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ACL 授权主体配置 Service 实现类
 *
 * @author IIMS
 */
@Service
@RequiredArgsConstructor
public class AclPrincipalTypeConfigServiceImpl implements AclPrincipalTypeConfigService {

    private final AclPrincipalTypeConfigMapper aclPrincipalTypeConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePrincipalTypes(AclPrincipalTypeOwnerType ownerType, Long ownerId,
                                   Set<PrincipalType> principalTypes) {
        // 先物理删除旧配置，再插入单行掩码，保证整体覆盖
        aclPrincipalTypeConfigMapper.deleteByOwner(ownerType.getValue(), ownerId);
        if (CollUtil.isEmpty(principalTypes)) {
            return;
        }
        AclPrincipalTypeConfigDO config = AclPrincipalTypeConfigDO.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .principalTypeMask(PrincipalType.toMask(principalTypes))
                .build();
        aclPrincipalTypeConfigMapper.insert(config);
    }

    @Override
    public Set<PrincipalType> getPrincipalTypes(AclPrincipalTypeOwnerType ownerType, Long ownerId) {
        if (ownerId == null) {
            return Collections.emptySet();
        }
        AclPrincipalTypeConfigDO config = aclPrincipalTypeConfigMapper.selectByOwner(ownerType, ownerId);
        if (config == null || config.getPrincipalTypeMask() == null) {
            return Collections.emptySet();
        }
        return PrincipalType.fromMask(config.getPrincipalTypeMask());
    }

    @Override
    public Set<PrincipalType> getPrincipalTypes(AclPrincipalTypeOwnerType ownerType,
                                                Collection<Long> ownerIds) {
        if (CollUtil.isEmpty(ownerIds)) {
            return new HashSet<>();
        }
        int mask = 0;
        for (AclPrincipalTypeConfigDO config : aclPrincipalTypeConfigMapper.selectByOwner(ownerType, ownerIds)) {
            if (config.getPrincipalTypeMask() != null) {
                mask |= config.getPrincipalTypeMask();
            }
        }
        return PrincipalType.fromMask(mask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByOwner(AclPrincipalTypeOwnerType ownerType, Long ownerId) {
        if (ownerId != null) {
            aclPrincipalTypeConfigMapper.deleteByOwner(ownerType.getValue(), ownerId);
        }
    }

}
