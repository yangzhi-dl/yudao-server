package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueContentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 对话内容 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiChatDialogueContentMapper extends BaseMapperX<AiChatDialogueContentDO> {

    /**
     * 批量插入对话内容
     */
    int batchInsert(@Param("list") List<AiChatDialogueContentDO> list);

    /**
     * 根据对话ID查询内容
     */
    default AiChatDialogueContentDO selectByDialogueId(Long dialogueId) {
        return selectOne(new LambdaQueryWrapperX<AiChatDialogueContentDO>()
                .eq(AiChatDialogueContentDO::getDialogueId, dialogueId));
    }

    /**
     * 根据对话ID列表批量查询内容
     */
    default List<AiChatDialogueContentDO> selectByDialogueIds(List<Long> dialogueIds) {
        return selectList(new LambdaQueryWrapperX<AiChatDialogueContentDO>()
                .in(AiChatDialogueContentDO::getDialogueId, dialogueIds));
    }

    /**
     * 根据对话ID删除内容
     */
    default int deleteByDialogueId(Long dialogueId) {
        return delete(new LambdaQueryWrapperX<AiChatDialogueContentDO>()
                .eq(AiChatDialogueContentDO::getDialogueId, dialogueId));
    }

    /**
     * 根据对话ID列表批量删除内容
     */
    default int deleteByDialogueIds(List<Long> dialogueIds) {
        return delete(new LambdaQueryWrapperX<AiChatDialogueContentDO>()
                .in(AiChatDialogueContentDO::getDialogueId, dialogueIds));
    }

}
