package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiSkillMapper;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateSkillDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SkillPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateSkillDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiSkillService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AiSkillServiceImpl implements AiSkillService {

    private final AiSkillMapper aiSkillMapper;

    private final FileApi fileApi;

    private final DictDataService dictDataService;

    private final static Integer EXPIRATION_SECONDS = 3000;

    public AiSkillServiceImpl(AiSkillMapper aiSkillMapper, FileApi fileApi, DictDataService dictDataService) {
        this.aiSkillMapper = aiSkillMapper;
        this.fileApi = fileApi;
        this.dictDataService = dictDataService;
    }

    @Override
    public PageResult<SkillVO> pageQuery(SkillPageQueryDTO dto) {
        PageResult<AiSkillDO> skillPage = aiSkillMapper.pageQuery(dto);
        List<SkillVO> list = new ArrayList<>();
        skillPage.getList().forEach(skill -> {
            SkillVO vo = SkillVO.builder().build();
            BeanUtils.copyProperties(skill, vo);
            if (skill.getIcon() != null) {
                vo.setImageUrl(fileApi.presignGetUrl(skill.getIcon(), EXPIRATION_SECONDS));
            }
            DictDataDO dictDataDO = dictDataService.getDictData(skill.getCategoryId());
            if (Objects.nonNull(dictDataDO)) {
                vo.setCategoryName(dictDataDO.getLabel());
            }
            List<Long> tagIds = skill.getTagIds();
            if (Objects.nonNull(tagIds)) {
                List<String> tagsName = new ArrayList<>();
                List<DictDataDO> dictDataByIds = dictDataService.getDictDataByIds(tagIds);
                dictDataByIds.forEach(dictTagId -> tagsName.add(dictTagId.getValue()));
                vo.setTagNames(tagsName);
            }
            list.add(vo);
        });
        return new PageResult<>(list, skillPage.getTotal());
    }

    @Override
    public Boolean insert(CreateSkillDTO dto) {
        AiSkillDO skillDO = AiSkillDO.builder()
                .name(dto.getName()).displayName(dto.getDisplayName())
                .description(dto.getDescription()).skillMdContent(dto.getSkillMdContent())
                .icon(dto.getIcon()).categoryId(dto.getCategoryId())
                .tagIds(dto.getTagIds()).version(dto.getVersion())
                .status(dto.getStatus()).build();
        return aiSkillMapper.insert(skillDO) > 0;
    }

    @Override
    public Boolean updateData(UpdateSkillDTO dto) {
        return aiSkillMapper.updateById(AiSkillDO.builder()
                .id(dto.getId()).name(dto.getName()).displayName(dto.getDisplayName())
                .description(dto.getDescription()).skillMdContent(dto.getSkillMdContent())
                .icon(dto.getIcon()).categoryId(dto.getCategoryId())
                .tagIds(dto.getTagIds()).version(dto.getVersion())
                .status(dto.getStatus()).build()) > 0;
    }

    @Override
    public Boolean delete(List<Long> ids) {
        return aiSkillMapper.delete(ids);
    }

    @Override
    public SkillDetailVO selectSkillById(Long id) {
        AiSkillDO skillDO = aiSkillMapper.selectById(id);
        Long icon = skillDO.getIcon();
        SkillDetailVO vo = SkillDetailVO.builder().tagIds(skillDO.getTagIds())
                .imageUrl(Objects.nonNull(icon)
                        ? fileApi.presignGetUrl(icon, EXPIRATION_SECONDS) : null).build();
        BeanUtils.copyProperties(skillDO, vo);
        return vo;
    }

}