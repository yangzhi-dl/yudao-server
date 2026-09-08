package cn.iocoder.yudao.module.hub.core.portal.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentPageReqVO;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentSaveReqVO;
import cn.iocoder.yudao.module.hub.core.portal.dal.dataobject.HubPortalContentDO;
import jakarta.validation.Valid;

import java.util.List;

public interface HubPortalContentService {

    Long createPortalContent(@Valid HubPortalContentSaveReqVO createReqVO);

    void updatePortalContent(@Valid HubPortalContentSaveReqVO updateReqVO);

    void deletePortalContent(Long id);

    HubPortalContentDO getPortalContent(Long id);

    PageResult<HubPortalContentDO> getPortalContentPage(HubPortalContentPageReqVO pageReqVO);

    List<HubPortalContentDO> getEnabledContentList(String pageCode);

}
