package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillResourceDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiSkillResourceMapper;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateSkillResourceDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillResourceVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiSkillResourceService;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AiSkillResourceServiceImpl implements AiSkillResourceService {

    private final AiSkillResourceMapper aiSkillResourceMapper;

    private final FileService fileService;

    private final static Integer EXPIRATION_SECONDS = 3000;

    public AiSkillResourceServiceImpl(AiSkillResourceMapper aiSkillResourceMapper, FileService fileService) {
        this.aiSkillResourceMapper = aiSkillResourceMapper;
        this.fileService = fileService;
    }

    @Override
    public List<SkillResourceVO> selectListBySkillId(Long skillId) {
        List<AiSkillResourceDO> resources = aiSkillResourceMapper.selectListBySkillId(skillId);
        List<SkillResourceVO> vos = new ArrayList<>();
        resources.forEach(resource -> {
            SkillResourceVO vo = SkillResourceVO.builder()
                    .id(resource.getId())
                    .skillId(resource.getSkillId())
                    .resourceType(resource.getResourceType())
                    .fileId(resource.getFileId())
                    .sortOrder(resource.getSortOrder())
                    .build();
            if (Objects.nonNull(resource.getFileId())) {
                try {
                    FileDO file = fileService.getFile(resource.getFileId());
                    vo.setFileName(file.getName());
                    vo.setFileUrl(fileService.presignGetUrl(file, EXPIRATION_SECONDS));
                } catch (Exception e) {
                   log.info(e.getMessage());
                }
            }
            vos.add(vo);
        });
        return vos;
    }

    @Override
    public Boolean insert(CreateSkillResourceDTO dto) {
        AiSkillResourceDO resourceDO = AiSkillResourceDO.builder()
                .skillId(dto.getSkillId())
                .resourceType(dto.getResourceType())
                .fileId(dto.getFileId())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        return aiSkillResourceMapper.insert(resourceDO) > 0;
    }

    @Override
    public Boolean delete(List<Long> ids) {
        return aiSkillResourceMapper.delete(ids);
    }

}