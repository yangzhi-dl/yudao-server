package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.module.ai.core.chat.context.PromptTemplateContext;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelChatOptions;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelSettingDO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatSettingService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatModelService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiModelToolService;
import cn.iocoder.yudao.module.ai.core.rag.utils.PromptTemplateUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class AiModelToolServiceImpl implements AiModelToolService {

    private final AiChatModelService aiChatModelService;

    private final AiChatSettingService settingService;

    private final FileService fileService;

    public AiModelToolServiceImpl(AiChatModelService aiChatModelService,
                                  AiChatSettingService settingService, FileService fileService) {
        this.aiChatModelService = aiChatModelService;
        this.settingService = settingService;
        this.fileService = fileService;
    }

    @Override
    public String imageOcrByModel(List<Long> fileIds) {
        List<Message> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage(
        """
                你是一名专业的OCR信息提取助手，任务是从图片中识别并提取所有关键信息，且生成的键值对必须是出自于原图片内容，并将其结构化为JSON格式输出。
                请根据图片内容的逻辑层次使用合适嵌套的JSON对象来表示。务必保证所提取的信息忠实于原图片内容，严禁伪造或臆造信息。
                注意：在解析过程中，保持对信息位置和逻辑关系的敏感度，以确保生成的JSON结构尽可能接近原始内容布局。
                """
        );
        ArrayList<Media> media = new ArrayList<>();
        if (!fileIds.isEmpty()) {
            List<FileDO> files = fileService.getFileByIds(fileIds);
            files.forEach(file -> {
                try {
                    byte[] fileContent = fileService.getFileContent(file.getConfigId(), file.getPath());
                    media.add(new Media(MimeTypeUtils.ALL,
                            new InputStreamResource(new ByteArrayInputStream(fileContent))));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        UserMessage userMessage = UserMessage.builder().text("输出结构化的JSON内容").media(media).build();
        messages.add(systemMessage);
        messages.add(userMessage);
        ModelSettingDO modelSetting = settingService.getUserModelSetting();
        ChatResponse call = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                .call(new Prompt(messages));
        return Objects.requireNonNull(call.getResult()).getOutput().getText();
    }

    @Override
    public String generateSummaryByModel(List<Document> documents) {
        // 定义阈值
        int threshold = 60; // 单次摘要的最大长度
        int maxContentLength = 10000; // 合并内容的最大长度（避免超出模型输入限制）

        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder mergedContentBuilder = new StringBuilder();
        int index = 1;

        for (Document document : documents) {
            String content = Objects.requireNonNull(document.getText());

            // 检查合并内容是否超过最大长度
            if (mergedContentBuilder.length() + content.length() <= maxContentLength) {
                // 如果未超过最大长度，将当前文档内容添加到合并内容中
                mergedContentBuilder.append(content).append("\n");
            } else {
                // 如果超过最大长度，处理已合并的内容
                processMergedContent(mergedContentBuilder, contentBuilder, index, threshold);

                // 清空合并内容，并将当前文档内容作为新的起点
                mergedContentBuilder.setLength(0);
                mergedContentBuilder.append(content).append("\n");
                index++;
            }
        }

        // 处理剩余的合并内容（如果还有未处理的部分）
        if (!mergedContentBuilder.isEmpty()) {
            processMergedContent(mergedContentBuilder, contentBuilder, index, threshold);
        }

        // 根据所有片段摘要生成最终的文档摘要
        Message finalMessage = new PromptTemplate("""
            所有的文档片段摘要都来自于同一个文档，请保持完整性、连贯性、真实性。
            ---所有文档片段摘要----------
            {content}
            ---------------------
            根据上面的文档的所有片段摘要生成一个不超过360个字的文档摘要，直接输出文档摘要。
            """
        ).createMessage(Map.of("content", contentBuilder.toString()));

        ModelSettingDO modelSetting = settingService.getUserModelSetting();
        ChatResponse finalCall = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                .call(new Prompt(finalMessage));
        return PromptTemplateUtil.removeThink(Objects.requireNonNull(finalCall.getResult()).getOutput().getText());
    }

    @Override
    public String generateTitleByModel(String summary) {
        Message finalMessage = new PromptTemplate("""
        请根据以下文档摘要生成一个简洁、准确的文档标题。
    
        文档摘要：
        ---
        {summary}
        ---
    
        要求：
        1. 标题长度控制在5-15个字之间
        2. 准确概括文档核心主题
        3. 使用陈述句，避免疑问句和感叹句
        4. 不要包含"关于"、"浅谈"等冗余词汇
    
        请只输出标题，不要输出任何额外内容。
        """).createMessage(Map.of("summary", summary));

        ModelSettingDO modelSetting = settingService.getUserModelSetting();
        ChatResponse finalCall = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                .call(new Prompt(finalMessage));
        return PromptTemplateUtil.removeThink(Objects.requireNonNull(finalCall.getResult()).getOutput().getText());
    }

    @Override
    public <C, T> T generateTaxonomy(C citation, Class<T> typeRef, String summary) {
        try {
            if (citation == null || typeRef == null) {
                throw new IllegalArgumentException("citation and typeRef cannot be null");
            }

            // 创建 BeanOutputConverter 用于格式化输出
            BeanOutputConverter<T> beanOutputConverter = new BeanOutputConverter<>(typeRef);

            // 获取 JSON schema 格式
            String format = beanOutputConverter.getFormat();

            // 将 citation 对象转换为 JSON 字符串
            String citationJson = JSON.toJSONString(citation);

            // 获取模型设置
            ModelSettingDO modelSetting = settingService.getUserModelSetting();

            // 创建系统提示消息
            Message sysMessage = new SystemPromptTemplate(PromptTemplateContext.TAXONOMY_SYSTEM)
                    .createMessage(Map.of(
                            "format", format,
                            "citation", citationJson,
                            "summary", summary
                    ));

            // 构建提示模板
            Message message = new PromptTemplate(PromptTemplateContext.TAXONOMY_USER)
                    .createMessage(Map.of("summary", summary));

            Prompt prompt = new Prompt(List.of(sysMessage, message));
            Generation generation = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                    .call(prompt)
                    .getResult();
            if (Objects.isNull(generation)) {
                return typeRef.getDeclaredConstructor().newInstance();
            }
            // 将生成的文本转换回目标对象
            AssistantMessage output = generation.getOutput();
            String text = output.getText();
            if (Objects.nonNull(text)) {
                return beanOutputConverter.convert(text);
            }
            return typeRef.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Failed to generate taxonomy", e);
            throw new RuntimeException("Failed to generate taxonomy: " + e.getMessage(), e);
        }
    }

    @Override
    public <C, T> T generateNewJSON(C citation, Class<T> typeRef, String context) {
        try {
            if (citation == null || typeRef == null) {
                throw new IllegalArgumentException("citation and typeRef cannot be null");
            }

            // 创建 BeanOutputConverter 用于格式化输出
            BeanOutputConverter<T> beanOutputConverter = new BeanOutputConverter<>(typeRef);

            String format = JSON.toJSONString(citation, SerializerFeature.WriteMapNullValue);

            // 获取模型设置
            ModelSettingDO modelSetting = settingService.getUserModelSetting();

            // 调用模型生成内容
            Generation generation = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                    .call(PromptTemplate.builder()
                            .template(PromptTemplateContext.JSON_USER)
                            .variables(Map.of(
                                    "format", format,
                                    "context", context
                            ))
                            .build()
                            .create())
                    .getResult();

            if (Objects.isNull(generation)) {
                return typeRef.getDeclaredConstructor().newInstance();
            }
            // 将生成的文本转换回目标对象
            AssistantMessage output = generation.getOutput();
            String text = output.getText();
            if (Objects.nonNull(text)) {
                return beanOutputConverter.convert(text);
            }
            return typeRef.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Failed to generate taxonomy", e);
            throw new RuntimeException("Failed to generate taxonomy: " + e.getMessage(), e);
        }
    }

    @Override
    public String optimizeMarkdownContent(String markdown, String prompt) {
        List<Message> messages = new ArrayList<>();

        SystemMessage systemMessage = new SystemMessage("""
            你是一名 Markdown 内容优化专家。

            核心原则：
            1. 保留原文核心信息，禁止编造、删改或遗漏重要内容。
            2. 自动去除页头、页脚、导航、版权声明等明显与正文无关的干扰信息。
            3. 尊重用户输入的优化指令（prompt），将其作为风格/结构/侧重方向的指导。
            4. 合理使用 Markdown 语法：标题（#）、列表（- / 1.）、粗体（**）、表格。
            5. 段落之间保留一个空行，排版整洁。
            6. 只输出 Markdown 文本，不要输出 ```markdown 或其它代码块包裹。
            7. 不要输出空内容。
            8. 当`输出模板`中出现`xxx`，代表要生成的对应的中文内容。
            """);

        Message userMessage = new PromptTemplate("""
            【用户优化指令】
            {prompt}

            【原始 Markdown 内容】
            {content}

            【要求】
            - 根据上面的“用户优化指令”对原始内容进行优化
            - 不增加、不虚构原文没有的信息
            - 不输出任何解释、前缀、后缀或对话内容，仅输出优化后的 Markdown
            """
        ).createMessage(Map.of(
                "prompt", prompt,
                "content", markdown
        ));

        messages.add(systemMessage);
        messages.add(userMessage);

        ModelSettingDO modelSetting = settingService.getUserModelSetting();
        ChatResponse call = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                .call(new Prompt(messages));
        return Objects.requireNonNull(call.getResult()).getOutput().getText();
    }

    @Override
    public String formattingDocument(String content) {
        List<Message> messages = new ArrayList<>();

        SystemMessage systemMessage = new SystemMessage("""
        你是一名 Markdown 排版修复专家，专注于清理和重组文档的物理布局，绝不涉及内容创作或语义修改。
        
        【核心原则】
        1. 绝对零修改：不允许改变原文的任何一个字、数字或标点符号。
        2. 只动结构：仅允许调整换行、空格、缩进、对齐和 Markdown 标记符号。
        3. 保留原意：不概括、不总结、不重写、不调整语序。
        
        【文档结构自动识别与修复规则】
        1. 标题层级诊断与修复：
           - 扫描全文，识别标题模式（如"第X章"、"第X条"、"一、"、"1." 等）。
           - 将文档主标题（通常是第一行或最突出的标题）设为 `# 标题`。
           - 将章节标题（"第X章"、"第一章"等）设为 `## 章节名`。
           - 将条款标题（"第X条"、"一、"、"1." 等）设为 `**标题**`（加粗文本，非标题标记）。
           - 将子章节标题（"（一）"、"(1)" 等）设为 `### 子标题`（仅在确实需要三级结构时使用）。
           - 确保标题层级连贯，不跳级（# → ## → ###）。
        
        2. 列表识别与统一：
           - 识别无序列表（-、*、•）和有序列表（1.、一.、(一) 等）。
           - 同级列表使用统一的缩进（2或4个空格）和标记符号。
           - 嵌套列表保持层级清晰，每层缩进递增。
        
        3. 段落与换行处理：
           - 将同一段落内不必要的硬回车合并为单行。
           - 保留标题、列表项、代码块、表格、引用块前后的换行。
           - 删除每行开头和结尾的无意义空格。
        
        4. 标点与空格规范：
           - 中文标点后不加多余空格。
           - 英文单词间保留一个空格，英文标点后保留一个空格。
           - 数字与单位间按语言习惯处理（中文：不加空格；英文：加空格）。
        
        5. 特殊结构处理：
           - 代码块：使用 ``` 包裹，可识别时标注语言。
           - 引用块：使用 > 标记。
           - 表格：保持 Markdown 表格格式（| 和 ---），不修改内容。
           - 分隔线：使用 --- 或 ***。
        
        6. 杂散标记清理：
           - 删除页码、页眉页脚、版面标记（如 "-3"、"- 9"、"---" 等冗余分隔）。
        
        【红线——绝对禁止】
        - 不得修改原文任何文字内容（汉字、数字、英文、标点符号均不可改动）。
        - 不得添加、删除或重组任何信息内容。
        - 不得添加任何解释、总结、评注、开场白或结尾语。
        - 不得使用代码块（如 ```markdown）包裹输出内容。
        - 输出必须是纯 Markdown 文本，直接从正文开始。
        """);

        Message userMessage = new PromptTemplate("""
        【原始文档内容】
        {content}
        
        【输出要求】
        - 仅输出整理后的完整 Markdown 文本。
        - 不添加任何开场白、解释或结尾总结。
        - 不包裹代码块，直接输出纯文本内容。
        - 从第一行开始就是修复后的文档正文。
        """
        ).createMessage(Map.of("content", content));

        messages.add(systemMessage);
        messages.add(userMessage);

        ModelSettingDO modelSetting = settingService.getUserModelSetting();
        ChatResponse call = aiChatModelService.getChatModel(modelSetting.getLanguageModelId(),
                        ModelChatOptions.builder().temperature(0.0).build())
                .call(new Prompt(messages));
        return Objects.requireNonNull(call.getResult()).getOutput().getText();
    }

    /**
     * 处理合并的内容，生成摘要并追加到结果中
     */
    private void processMergedContent(StringBuilder mergedContentBuilder, StringBuilder contentBuilder, int index, int threshold) {
        String mergedContent = mergedContentBuilder.toString();
        if (StringUtils.isBlank(mergedContent)) return;

        // 调用模型生成摘要
        Message message = new PromptTemplate("""
            请保持完整性、连贯性、真实性，且不能丢失关键描述。
            ---文档片段----------
            {content}
            ---------------------
            根据上面的文档片段生成一个不超过{threshold}个字的摘要，直接输出摘要：
            """
        ).createMessage(Map.of("content", mergedContent, "threshold", threshold));

        ModelSettingDO modelSetting = settingService.getUserModelSetting();
        ChatResponse call = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                .call(new Prompt(message));
        String result = PromptTemplateUtil.removeThink(Objects.requireNonNull(call.getResult()).getOutput().getText());

        // 将生成的摘要追加到结果中
        String text = "# 当前文档的第" + index + "个片段摘要：";
        contentBuilder.append(text).append(result).append("\n\n");
    }

}
