package cn.iocoder.yudao.module.hub.core.portal.service.impl;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentPageReqVO;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentSaveReqVO;
import cn.iocoder.yudao.module.hub.core.portal.dal.dataobject.HubPortalContentDO;
import cn.iocoder.yudao.module.hub.core.portal.dal.mysql.HubPortalContentMapper;
import cn.iocoder.yudao.module.hub.core.portal.service.HubPortalContentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hub.core.portal.enums.ErrorCodeConstants.PORTAL_CONTENT_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.hub.core.portal.enums.ErrorCodeConstants.PORTAL_CONTENT_NOT_EXISTS;

@Service
@Validated
public class HubPortalContentServiceImpl implements HubPortalContentService {

    @Resource
    private HubPortalContentMapper portalContentMapper;

    @Override
    public Long createPortalContent(HubPortalContentSaveReqVO createReqVO) {
        validatePortalContentCodeUnique(null, createReqVO.getPageCode(), createReqVO.getContentType(),
                createReqVO.getCode());
        HubPortalContentDO portalContent = BeanUtils.toBean(createReqVO, HubPortalContentDO.class);
        portalContentMapper.insert(portalContent);
        return portalContent.getId();
    }

    @Override
    public void updatePortalContent(HubPortalContentSaveReqVO updateReqVO) {
        validatePortalContentExists(updateReqVO.getId());
        validatePortalContentCodeUnique(updateReqVO.getId(), updateReqVO.getPageCode(),
                updateReqVO.getContentType(), updateReqVO.getCode());
        portalContentMapper.updateById(BeanUtils.toBean(updateReqVO, HubPortalContentDO.class));
    }

    @Override
    public void deletePortalContent(Long id) {
        validatePortalContentExists(id);
        portalContentMapper.deleteById(id);
    }

    private void validatePortalContentExists(Long id) {
        if (id == null || portalContentMapper.selectById(id) == null) {
            throw exception(PORTAL_CONTENT_NOT_EXISTS);
        }
    }

    private void validatePortalContentCodeUnique(Long id, String pageCode, String contentType, String code) {
        HubPortalContentDO portalContent = portalContentMapper
                .selectByPageCodeAndContentTypeAndCode(pageCode, contentType, code);
        if (portalContent != null && !portalContent.getId().equals(id)) {
            throw exception(PORTAL_CONTENT_CODE_DUPLICATE, pageCode, contentType, code);
        }
    }

    @Override
    public HubPortalContentDO getPortalContent(Long id) {
        return portalContentMapper.selectById(id);
    }

    @Override
    public PageResult<HubPortalContentDO> getPortalContentPage(HubPortalContentPageReqVO pageReqVO) {
        return portalContentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HubPortalContentDO> getEnabledContentList(String pageCode) {
        return portalContentMapper.selectListByPageCodeAndStatus(pageCode, CommonStatusEnum.ENABLE.getStatus());
    }

}
