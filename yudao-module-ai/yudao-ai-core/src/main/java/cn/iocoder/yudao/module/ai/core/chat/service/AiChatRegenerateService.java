package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.module.ai.core.chat.model.vo.RegenerateVersionVO;

import java.util.Collection;
import java.util.List;

/**
 * AI 回答重新生成版本 Service
 * <p>
 * 版本表存储全部回答版本（含当前生效版本）：version 为第几次回答（1 起），
 * is_current 标记当前生效版本；ai_chat_dialogue 主行始终保存当前版本内容，
 * 作为消息链锚点（上下文/导出/收藏/反馈无感）。
 *
 * @author yudao
 */
public interface AiChatRegenerateService {

    /**
     * 保存重新生成的新版本：
     * 首次重生成时自动把主行当前内容补为 version=1（原始回答），
     * 新回答登记为 version=max+1 并标记为当前生效版本
     */
    void saveRegenerateVersion(Long dialogueId, String content, String metadata, String tools);

    /**
     * 查询指定 assistant 对话的版本列表（按版本序号升序，第 1 个为第 1 次回答）
     * <p>
     * 兼容旧数据：版本表无当前版本标记时，主行当前内容作为最新版本追加（isCurrent = true）
     */
    List<RegenerateVersionVO> listVersions(Long dialogueId);

    /**
     * 查询指定话题下所有 assistant 对话的版本列表（前端按 dialogueId 分组）
     */
    List<RegenerateVersionVO> listVersionsByTopic(Long topicId);

    /**
     * 切换版本：主行内容同步为目标版本内容，目标版本行标记为当前生效版本，返回切换后的完整版本列表
     */
    List<RegenerateVersionVO> switchVersion(Long dialogueId, Long versionId);

    /**
     * 按 assistant 对话 ID 软删版本（删除消息时级联）
     */
    void deleteByDialogueId(Long dialogueId);

    /**
     * 按 assistant 对话 ID 列表批量软删版本
     */
    void deleteByDialogueIds(Collection<Long> dialogueIds);

    /**
     * 按用户消息 ID（问题锚点）软删版本
     */
    void deleteByLastId(Long lastId);

    /**
     * 按话题 ID 列表批量软删版本
     */
    void deleteByTopicIds(Collection<Long> topicIds);

    /**
     * 按创建者软删版本（清空全部对话时使用）
     */
    void deleteByCreator(String creator, Collection<Long> ignoreTopicIds);

}
