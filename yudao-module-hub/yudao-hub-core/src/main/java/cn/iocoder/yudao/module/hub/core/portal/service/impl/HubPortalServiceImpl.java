package cn.iocoder.yudao.module.hub.core.portal.service.impl;

import cn.iocoder.yudao.module.hub.core.portal.controller.app.vo.HubHomeRespVO;
import cn.iocoder.yudao.module.hub.core.portal.dal.dataobject.HubPortalContentDO;
import cn.iocoder.yudao.module.hub.core.portal.enums.HubPortalContentTypeEnum;
import cn.iocoder.yudao.module.hub.core.portal.service.HubPortalContentService;
import cn.iocoder.yudao.module.hub.core.portal.service.HubPortalService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Validated
public class HubPortalServiceImpl implements HubPortalService {

    private static final String PORTAL_PAGE_CODE = "portal";

    @Resource
    private HubPortalContentService portalContentService;

    @Override
    public HubHomeRespVO getHome() {
        List<HubPortalContentDO> contentList = portalContentService.getEnabledContentList(PORTAL_PAGE_CODE);
        HubHomeRespVO response = new HubHomeRespVO();
        response.setPageCode(PORTAL_PAGE_CODE);
        response.setSections(new ArrayList<>());
        response.setCapabilities(new ArrayList<>());
        response.setProducts(new ArrayList<>());
        response.setSolutions(new ArrayList<>());
        response.setCases(new ArrayList<>());
        for (HubPortalContentDO content : contentList) {
            mapContent(response, content);
        }
        attachProductMedia(response, contentList);
        attachSolutionMedia(response, contentList);
        return response;
    }

    private void mapContent(HubHomeRespVO response, HubPortalContentDO content) {
        String contentType = content.getContentType();
        if (HubPortalContentTypeEnum.HERO.getType().equals(contentType)) {
            if (response.getHero() == null) {
                response.setHero(mapHero(content));
            }
        } else if (HubPortalContentTypeEnum.SECTION.getType().equals(contentType)) {
            response.getSections().add(mapSection(content));
        } else if (HubPortalContentTypeEnum.CAPABILITY.getType().equals(contentType)) {
            response.getCapabilities().add(mapCapability(content));
        } else if (HubPortalContentTypeEnum.PRODUCT.getType().equals(contentType)) {
            response.getProducts().add(mapProduct(content));
        } else if (HubPortalContentTypeEnum.SOLUTION.getType().equals(contentType)) {
            response.getSolutions().add(mapSolution(content));
        } else if (HubPortalContentTypeEnum.CASE.getType().equals(contentType)) {
            response.getCases().add(mapCase(content));
        } else if (HubPortalContentTypeEnum.PRIVATE_DEPLOYMENT.getType().equals(contentType)) {
            if (response.getPrivateDeployment() == null) {
                response.setPrivateDeployment(mapPrivateDeployment(content));
            }
        } else if (HubPortalContentTypeEnum.CTA.getType().equals(contentType)
                && response.getCallToAction() == null) {
            response.setCallToAction(mapCallToAction(content));
        }
    }

    private HubHomeRespVO.HeroVO mapHero(HubPortalContentDO content) {
        HubHomeRespVO.HeroVO hero = new HubHomeRespVO.HeroVO();
        hero.setEyebrow(content.getEyebrow());
        hero.setTitle(content.getTitle());
        hero.setSummary(content.getSummary());
        hero.setImageUrl(content.getImageUrl());
        hero.setActionText(content.getActionText());
        hero.setActionUrl(content.getActionUrl());
        hero.setSecondaryActionText(content.getSecondaryActionText());
        hero.setSecondaryActionUrl(content.getSecondaryActionUrl());
        hero.setProofPoints(content.getHighlights());
        return hero;
    }

    private HubHomeRespVO.SectionVO mapSection(HubPortalContentDO content) {
        HubHomeRespVO.SectionVO section = new HubHomeRespVO.SectionVO();
        section.setCode(content.getCode());
        section.setEyebrow(content.getEyebrow());
        section.setTitle(content.getTitle());
        section.setSummary(content.getSummary());
        section.setDescription(content.getDescription());
        return section;
    }

    private HubHomeRespVO.CapabilityVO mapCapability(HubPortalContentDO content) {
        HubHomeRespVO.CapabilityVO capability = new HubHomeRespVO.CapabilityVO();
        capability.setCode(content.getCode());
        capability.setEyebrow(content.getEyebrow());
        capability.setTitle(content.getTitle());
        capability.setDescription(content.getDescription());
        capability.setImageUrl(content.getImageUrl());
        capability.setHighlights(content.getHighlights());
        return capability;
    }

    private HubHomeRespVO.ProductVO mapProduct(HubPortalContentDO content) {
        HubHomeRespVO.ProductVO product = new HubHomeRespVO.ProductVO();
        product.setCode(content.getCode());
        product.setTitle(content.getTitle());
        product.setSummary(content.getSummary());
        product.setDescription(content.getDescription());
        product.setHighlights(content.getHighlights());
        product.setImageUrl(content.getImageUrl());
        product.setMedia(new ArrayList<>());
        product.setActionText(content.getActionText());
        product.setActionUrl(content.getActionUrl());
        return product;
    }

    /**
     * 将产品素材归入对应产品，保证前台只消费结构化内容。
     */
    private void attachProductMedia(HubHomeRespVO response, List<HubPortalContentDO> contentList) {
        List<HubPortalContentDO> mediaList = contentList.stream()
                .filter(content -> HubPortalContentTypeEnum.PRODUCT_MEDIA.getType()
                        .equals(content.getContentType()))
                .toList();
        for (HubHomeRespVO.ProductVO product : response.getProducts()) {
            mediaList.stream()
                    .filter(media -> Objects.equals(product.getCode(), media.getParentCode()))
                    .map(this::mapProductMedia)
                    .forEach(product.getMedia()::add);
        }
    }

    private HubHomeRespVO.ProductMediaVO mapProductMedia(HubPortalContentDO content) {
        HubHomeRespVO.ProductMediaVO media = new HubHomeRespVO.ProductMediaVO();
        media.setCode(content.getCode());
        media.setTitle(content.getTitle());
        media.setSummary(content.getSummary());
        media.setImageUrl(content.getImageUrl());
        return media;
    }

    private HubHomeRespVO.SolutionVO mapSolution(HubPortalContentDO content) {
        HubHomeRespVO.SolutionVO solution = new HubHomeRespVO.SolutionVO();
        solution.setCode(content.getCode());
        solution.setIndustry(content.getEyebrow());
        solution.setTitle(content.getTitle());
        solution.setDescription(content.getDescription());
        solution.setScenario(content.getScenario());
        solution.setOutcome(content.getOutcome());
        solution.setFoundations(content.getHighlights());
        solution.setImageUrl(content.getImageUrl());
        solution.setMedia(new ArrayList<>());
        solution.setActionText(content.getActionText());
        solution.setActionUrl(content.getActionUrl());
        return solution;
    }

    /**
     * 将独立维护的解决方案素材归入对应解决方案，避免在服务实现中写死前台图片。
     */
    private void attachSolutionMedia(HubHomeRespVO response, List<HubPortalContentDO> contentList) {
        List<HubPortalContentDO> mediaList = contentList.stream()
                .filter(content -> HubPortalContentTypeEnum.SOLUTION_MEDIA.getType()
                        .equals(content.getContentType()))
                .toList();
        for (HubHomeRespVO.SolutionVO solution : response.getSolutions()) {
            mediaList.stream()
                    .filter(media -> Objects.equals(solution.getCode(), media.getParentCode()))
                    .map(this::mapSolutionMedia)
                    .forEach(solution.getMedia()::add);
        }
    }

    private HubHomeRespVO.SolutionMediaVO mapSolutionMedia(HubPortalContentDO content) {
        HubHomeRespVO.SolutionMediaVO media = new HubHomeRespVO.SolutionMediaVO();
        media.setCode(content.getCode());
        media.setLabel(content.getEyebrow());
        media.setTitle(content.getTitle());
        media.setSummary(content.getSummary());
        media.setImageUrl(content.getImageUrl());
        return media;
    }

    private HubHomeRespVO.CaseVO mapCase(HubPortalContentDO content) {
        HubHomeRespVO.CaseVO caseItem = new HubHomeRespVO.CaseVO();
        caseItem.setCode(content.getCode());
        caseItem.setLabel(content.getEyebrow());
        caseItem.setTitle(content.getTitle());
        caseItem.setDescription(content.getDescription());
        caseItem.setOutcome(content.getOutcome());
        caseItem.setImageUrl(content.getImageUrl());
        return caseItem;
    }

    private HubHomeRespVO.PrivateDeploymentVO mapPrivateDeployment(HubPortalContentDO content) {
        HubHomeRespVO.PrivateDeploymentVO deployment = new HubHomeRespVO.PrivateDeploymentVO();
        deployment.setTitle(content.getTitle());
        deployment.setDescription(content.getDescription());
        deployment.setImageUrl(content.getImageUrl());
        deployment.setHighlights(content.getHighlights());
        deployment.setActionText(content.getActionText());
        deployment.setActionUrl(content.getActionUrl());
        return deployment;
    }

    private HubHomeRespVO.CallToActionVO mapCallToAction(HubPortalContentDO content) {
        HubHomeRespVO.CallToActionVO callToAction = new HubHomeRespVO.CallToActionVO();
        callToAction.setEyebrow(content.getEyebrow());
        callToAction.setTitle(content.getTitle());
        callToAction.setSummary(content.getSummary());
        callToAction.setActionText(content.getActionText());
        callToAction.setActionUrl(content.getActionUrl());
        return callToAction;
    }

}
