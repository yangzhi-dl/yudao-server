package cn.iocoder.yudao.module.hub.core.portal.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentPageReqVO;
import cn.iocoder.yudao.module.hub.core.portal.dal.dataobject.HubPortalContentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HubPortalContentMapper extends BaseMapperX<HubPortalContentDO> {

    default PageResult<HubPortalContentDO> selectPage(HubPortalContentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HubPortalContentDO>()
                .eqIfPresent(HubPortalContentDO::getPageCode, reqVO.getPageCode())
                .eqIfPresent(HubPortalContentDO::getContentType, reqVO.getContentType())
                .likeIfPresent(HubPortalContentDO::getCode, reqVO.getCode())
                .likeIfPresent(HubPortalContentDO::getTitle, reqVO.getTitle())
                .eqIfPresent(HubPortalContentDO::getStatus, reqVO.getStatus())
                .orderByAsc(HubPortalContentDO::getSort)
                .orderByAsc(HubPortalContentDO::getId));
    }

    default HubPortalContentDO selectByPageCodeAndContentTypeAndCode(
            String pageCode, String contentType, String code) {
        return selectOne(new LambdaQueryWrapperX<HubPortalContentDO>()
                .eq(HubPortalContentDO::getPageCode, pageCode)
                .eq(HubPortalContentDO::getContentType, contentType)
                .eq(HubPortalContentDO::getCode, code));
    }

    default List<HubPortalContentDO> selectListByPageCodeAndStatus(String pageCode, Integer status) {
        return selectList(new LambdaQueryWrapperX<HubPortalContentDO>()
                .eq(HubPortalContentDO::getPageCode, pageCode)
                .eq(HubPortalContentDO::getStatus, status)
                .orderByAsc(HubPortalContentDO::getSort)
                .orderByAsc(HubPortalContentDO::getId));
    }

}
