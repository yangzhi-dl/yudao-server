package cn.iocoder.yudao.module.ai.core.rag.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownSplitter {

    // 默认最大块大小（字符数）
    private static final int DEFAULT_MAX_CHUNK_SIZE = 3000;
    // 块重叠大小（字符数），用于保持上下文连贯
    private static final int DEFAULT_OVERLAP_SIZE = 200;

    /**
     * 根据Markdown标题分块，使用默认块大小限制（3000字符）
     */
    public static List<Document> splitByHeadersWithHierarchy(String markdownText, Map<String, Object> metadata) {
        return splitByHeadersWithHierarchy(markdownText, metadata, DEFAULT_MAX_CHUNK_SIZE);
    }

    /**
     * 根据Markdown标题分块，包含从顶级标题到当前标题的所有内容，并跳过代码块中的内容。
     * 支持按字数限制智能拆分，保证句子和段落完整性。
     *
     * @param markdownText Markdown文本
     * @param metadata     元数据
     * @param maxChunkSize 最大块大小（字符数）
     */
    public static List<Document> splitByHeadersWithHierarchy(String markdownText, Map<String, Object> metadata, int maxChunkSize) {
        return splitByHeadersWithHierarchy(markdownText, metadata, maxChunkSize, DEFAULT_OVERLAP_SIZE);
    }

    /**
     * 根据Markdown标题分块，包含从顶级标题到当前标题的所有内容，并跳过代码块中的内容。
     * 支持按字数限制智能拆分，保证句子和段落完整性。
     *
     * @param markdownText Markdown文本
     * @param metadata     元数据
     * @param maxChunkSize 最大块大小（字符数）
     * @param overlapSize  块重叠大小（字符数）
     */
    public static List<Document> splitByHeadersWithHierarchy(String markdownText, Map<String, Object> metadata,
                                                             int maxChunkSize, int overlapSize) {
        // 预处理：移除 HTML 表格中的空行（仅包含空 <td> 的 <tr>），减少无意义的向量数据
        String cleanedText = removeEmptyTableRows(markdownText);

        // 首先按标题结构分块
        List<RawChunk> rawChunks = extractHeaderBasedChunks(cleanedText);

        // 如果没有提取到任何块（例如无标题的情况），将整个文档作为一个块处理
        if (rawChunks.isEmpty() && !cleanedText.trim().isEmpty()) {
            rawChunks.add(new RawChunk("", cleanedText.trim()));
        }

        // 对每个原始块按大小限制进行智能拆分
        List<Document> documents = new ArrayList<>();
        int documentIndex = 0;

        for (RawChunk rawChunk : rawChunks) {
            if (rawChunk.content.length() <= maxChunkSize) {
                // 块大小在限制内，直接添加
                documents.add(createDocument(rawChunk, metadata, documentIndex));
            } else {
                // 块大小超过限制，需要拆分
                List<RawChunk> subChunks = splitContentBySize(
                        rawChunk.headerPath,
                        rawChunk.content,
                        maxChunkSize,
                        overlapSize
                );

                for (RawChunk subChunk : subChunks) {
                    documents.add(createDocument(subChunk, metadata, documentIndex));
                }
            }
            documentIndex++;
        }

        return documents;
    }

    /**
     * 原始块数据结构
     */
    private static class RawChunk {
        String headerPath;  // 标题路径，如 "第一章 > 第一节 > 小节"
        String content;     // 块内容

        RawChunk(String headerPath, String content) {
            this.headerPath = headerPath;
            this.content = content;
        }
    }

    /**
     * 提取基于标题结构的原始块
     */
    private static List<RawChunk> extractHeaderBasedChunks(String markdownText) {
        // 统一换行符，避免 Windows CRLF 导致正则匹配失败
        String text = markdownText.replace("\r\n", "\n").replace("\r", "\n");

        Pattern headerPattern = Pattern.compile("^(#+)\\s+(.*)", Pattern.MULTILINE);
        Pattern codeBlockPattern = Pattern.compile("(```[\\s\\S]*?```)", Pattern.DOTALL);

        Matcher matcher = headerPattern.matcher(text);
        Matcher codeBlockMatcher = codeBlockPattern.matcher(text);

        List<RawChunk> rawChunks = new ArrayList<>();
        int lastMatchEnd = 0;
        List<String> headerStack = new ArrayList<>();
        StringBuilder currentContent = new StringBuilder();

        // 获取代码块范围
        List<int[]> codeBlockRanges = new ArrayList<>();
        while (codeBlockMatcher.find()) {
            codeBlockRanges.add(new int[]{codeBlockMatcher.start(), codeBlockMatcher.end()});
        }

        while (matcher.find()) {
            // 跳过代码块中的标题
            boolean isInCodeBlock = false;
            for (int[] range : codeBlockRanges) {
                if (matcher.start() >= range[0] && matcher.end() <= range[1]) {
                    isInCodeBlock = true;
                    break;
                }
            }
            if (isInCodeBlock) {
                continue;
            }

            // 保存上一个块
            if (!headerStack.isEmpty()) {
                String bodyContent = text.substring(lastMatchEnd, matcher.start()).trim();
                if (!bodyContent.isEmpty()) {
                    currentContent.append(bodyContent).append("\n");
                    String headerPath = String.join(" > ", headerStack);
                    rawChunks.add(new RawChunk(headerPath, currentContent.toString().trim()));
                    currentContent.setLength(0);
                }
            }

            // 更新标题栈
            String headerLevel = matcher.group(1);
            String headerText = matcher.group(2).trim();
            int level = headerLevel.length();

            if (level <= headerStack.size()) {
                headerStack = headerStack.subList(0, level - 1);
            }
            headerStack.add(headerText);
            lastMatchEnd = matcher.end();
        }

        // 处理最后一个块
        if (lastMatchEnd < text.length()) {
            String remainingContent = text.substring(lastMatchEnd).trim();
            if (!remainingContent.isEmpty() || !currentContent.isEmpty()) {
                currentContent.append(remainingContent);
            }
        }
        if (!headerStack.isEmpty() && !currentContent.isEmpty()) {
            String headerPath = String.join(" > ", headerStack);
            rawChunks.add(new RawChunk(headerPath, currentContent.toString().trim()));
        }

        return rawChunks;
    }

    /**
     * 按大小智能拆分内容，保证句子和段落完整性
     *
     * @param headerPath  标题路径
     * @param content     待拆分的内容
     * @param maxSize     最大块大小
     * @param overlapSize 重叠大小
     * @return 拆分后的块列表
     */
    private static List<RawChunk> splitContentBySize(String headerPath, String content,
                                                     int maxSize, int overlapSize) {
        List<RawChunk> chunks = new ArrayList<>();

        // 如果内容本身就不大，直接返回
        if (content.length() <= maxSize) {
            chunks.add(new RawChunk(headerPath, content));
            return chunks;
        }

        // 检测是否包含HTML表格（整段无换行）
        if (isCompleteHtmlTable(content)) {
            List<String> tableChunks = splitHtmlTableContent(content, maxSize);
            int chunkIndex = 1;
            for (String tableChunk : tableChunks) {
                String chunkHeaderPath = headerPath + (tableChunks.size() > 1 ? " (部分" + chunkIndex++ + ")" : "");
                chunks.add(new RawChunk(chunkHeaderPath, tableChunk));
            }
            return chunks;
        }

        // 检测是否包含Markdown表格
        if (isMarkdownTable(content)) {
            List<String> tableChunks = splitMarkdownTableContent(content, maxSize);
            int chunkIndex = 1;
            for (String tableChunk : tableChunks) {
                String chunkHeaderPath = headerPath + (tableChunks.size() > 1 ? " (部分" + chunkIndex++ + ")" : "");
                chunks.add(new RawChunk(chunkHeaderPath, tableChunk));
            }
            return chunks;
        }

        // 按段落拆分（保留段落结构）
        List<String> paragraphs = splitByParagraphs(content);

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 1;

        for (String paragraph : paragraphs) {
            String potentialChunk = !currentChunk.isEmpty()
                    ? currentChunk + "\n\n" + paragraph
                    : paragraph;

            if (potentialChunk.length() <= maxSize) {
                // 加上当前段落后仍不超过限制
                if (!currentChunk.isEmpty()) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            } else {
                // 超过限制，需要拆分
                if (!currentChunk.isEmpty()) {
                    // 保存当前块
                    String chunkHeaderPath = headerPath + " (部分" + chunkIndex++ + ")";
                    chunks.add(new RawChunk(chunkHeaderPath, currentChunk.toString()));
                }

                // 处理当前段落
                if (paragraph.length() > maxSize) {
                    // 单个段落就超过限制，需要按句子拆分
                    List<String> sentenceChunks = splitLongParagraph(paragraph, maxSize, overlapSize);
                    for (String sentenceChunk : sentenceChunks) {
                        String chunkHeaderPath = headerPath + " (部分" + chunkIndex++ + ")";
                        chunks.add(new RawChunk(chunkHeaderPath, sentenceChunk));
                    }
                    currentChunk.setLength(0);
                } else {
                    // 段落本身不超过限制，作为新块的开始
                    currentChunk.setLength(0);
                    currentChunk.append(paragraph);

                    // 添加上下文重叠
                    if (overlapSize > 0 && !chunks.isEmpty()) {
                        RawChunk lastChunk = chunks.get(chunks.size() - 1);
                        String overlap = extractOverlap(lastChunk.content, overlapSize);
                        if (!overlap.isEmpty()) {
                            currentChunk.insert(0, "> " + overlap.replace("\n", "\n> ") + "\n\n---\n\n");
                        }
                    }
                }
            }
        }

        // 保存最后一个块
        if (!currentChunk.isEmpty()) {
            String chunkHeaderPath = headerPath + (chunkIndex > 1 ? " (部分" + chunkIndex + ")" : "");
            chunks.add(new RawChunk(chunkHeaderPath, currentChunk.toString()));
        }

        return chunks;
    }

    /**
     * 检测是否是完整的HTML表格
     */
    private static boolean isCompleteHtmlTable(String text) {
        return text.contains("<table") && text.contains("</table>");
    }

    /**
     * 检测是否是Markdown表格
     */
    private static boolean isMarkdownTable(String text) {
        if (!text.contains("|")) return false;
        // 检查是否有分隔符行（如 |---|---|），用简单扫描代替正则
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n' || i == 0) {
                int lineStart = (i == 0) ? 0 : i + 1;
                int lineEnd = text.indexOf('\n', lineStart);
                if (lineEnd == -1) lineEnd = text.length();
                String line = text.substring(lineStart, lineEnd);
                // 分隔行特征：包含 | 且去掉 | : - 空格后为空
                String stripped = line.replace("|", "").replace(":", "").replace("-", "").replace(" ", "").trim();
                if (line.contains("|") && stripped.isEmpty()) {
                    return true;
                }
                i = lineEnd; // 跳到行末
            }
        }
        return false;
    }

    /**
     * 按段落拆分文本（保留空行分隔，兼容 \r\n 和 \n）。
     * 使用遍历方式避免大正则 split 导致的内存峰值。
     */
    private static List<String> splitByParagraphs(String text) {
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        List<String> paragraphs = new ArrayList<>();

        int len = normalized.length();
        int paraStart = 0;

        // 跳过开头的空行
        while (paraStart < len && normalized.charAt(paraStart) == '\n') {
            paraStart++;
        }

        for (int i = paraStart; i < len; i++) {
            if (normalized.charAt(i) == '\n') {
                // 检查是否为空行分隔：当前行之后紧跟 \n（或空白行后跟 \n）
                int j = i + 1;
                // 跳过本行后的空白符
                while (j < len && (normalized.charAt(j) == ' ' || normalized.charAt(j) == '\t')) {
                    j++;
                }
                if (j < len && normalized.charAt(j) == '\n') {
                    // 找到段落分隔：paraStart 到 i 是一个段落
                    String para = normalized.substring(paraStart, i).trim();
                    if (!para.isEmpty()) {
                        paragraphs.add(para);
                    }
                    // 跳过所有连续 \n 找到下一个段落开头
                    paraStart = j;
                    while (paraStart < len && normalized.charAt(paraStart) == '\n') {
                        paraStart++;
                    }
                    i = paraStart - 1; // for 循环会 i++，所以这里设到 paraStart-1
                }
                // 单 \n 是行内换行，继续扫描
            }
        }

        // 处理最后一个段落
        if (paraStart < len) {
            String para = normalized.substring(paraStart).trim();
            if (!para.isEmpty()) {
                paragraphs.add(para);
            }
        }

        return paragraphs;
    }

    /**
     * 处理超长段落，按句子拆分
     */
    private static List<String> splitLongParagraph(String paragraph, int maxSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();

        // 按句子拆分（保留句子完整性）
        List<String> sentences = splitBySentences(paragraph);

        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            String trimmedSentence = sentence.trim();
            if (trimmedSentence.isEmpty()) {
                continue;
            }

            String potentialChunk = !currentChunk.isEmpty()
                    ? currentChunk + " " + trimmedSentence
                    : trimmedSentence;

            if (potentialChunk.length() <= maxSize) {
                if (!currentChunk.isEmpty()) {
                    currentChunk.append(" ");
                }
                currentChunk.append(trimmedSentence);
            } else {
                // 保存当前块
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString());
                }

                // 开始新块
                currentChunk.setLength(0);

                // 如果单个句子就超过限制，强制拆分（按字符）
                if (trimmedSentence.length() > maxSize) {
                    List<String> forcedChunks = forceSplitByChar(trimmedSentence, maxSize, overlapSize);
                    chunks.addAll(forcedChunks);
                } else {
                    currentChunk.append(trimmedSentence);
                }
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * 拆分HTML表格内容（处理单行HTML表格）
     * 注意：超大表格可能触发正则回溯爆炸，需要限制拆分复杂度
     */
    private static List<String> splitHtmlTableContent(String content, int maxSize) {
        List<String> chunks = new ArrayList<>();

        // 如果整个表格不超过限制，直接返回
        if (content.length() <= maxSize) {
            chunks.add(content);
            return chunks;
        }

        // 快速估算表格行数：如果 </tr> 出现次数超过 50，说明是很大很复杂的表格，
        // 直接用简单方式拆分，避免深度正则回溯导致性能爆炸
        int trCount = countSubstring(content, "</tr>");
        if (trCount > 50) {
            return splitHtmlTableBySimpleTr(content, maxSize);
        }

        // 提取table标签及其属性
        Pattern tableStartPattern = Pattern.compile("<table[^>]*>");
        Matcher tableStartMatcher = tableStartPattern.matcher(content);
        String tableStart = "<table>";
        int tableStartEnd = 0;
        if (tableStartMatcher.find()) {
            tableStart = tableStartMatcher.group();
            tableStartEnd = tableStartMatcher.end();
        }

        // 提取表格内部内容（去掉外层table标签）
        String innerContent = content;
        if (tableStartEnd > 0) {
            innerContent = content.substring(tableStartEnd);
            if (innerContent.endsWith("</table>")) {
                innerContent = innerContent.substring(0, innerContent.length() - 8);
            }
        }

        // 提取每行（<tr>...</tr>），使用简单字符串匹配代替正则，避免大 HTML 回溯
        List<String> rows = new ArrayList<>();
        int searchPos = 0;
        while (searchPos < innerContent.length()) {
            int trStart = innerContent.indexOf("<tr", searchPos);
            if (trStart == -1) break;
            int trEnd = innerContent.indexOf("</tr>", trStart);
            if (trEnd == -1) break;
            trEnd += 5; // 包含 </tr>
            String row = innerContent.substring(trStart, trEnd);
            // 只保留以 <tr 开头的行（排除嵌套情况）
            if (row.trim().startsWith("<tr")) {
                rows.add(row);
            }
            searchPos = trEnd;
        }

        // 如果没有匹配到行，尝试用</tr>分割
        if (rows.isEmpty() && innerContent.contains("</tr>")) {
            String[] parts = innerContent.split("</tr>");
            for (String part : parts) {
                if (part.trim().isEmpty()) continue;
                String row = part + "</tr>";
                // 确保有<tr>标签
                if (!row.trim().startsWith("<tr") && row.contains("<tr")) {
                    int trStart = row.indexOf("<tr");
                    row = row.substring(trStart);
                }
                rows.add(row);
            }
        }

        // 如果还是没找到行，强制按字符拆分
        if (rows.isEmpty()) {
            return forceSplitByChar(content, maxSize, 0);
        }

        // 按行组拆分
        StringBuilder currentChunk = new StringBuilder();
        currentChunk.append(tableStart);

        int effectiveMaxSize = maxSize - 20; // 预留</table>的空间

        for (String row : rows) {
            String potentialChunk = currentChunk + row;

            if (potentialChunk.length() + 8 <= effectiveMaxSize) {
                currentChunk.append(row);
            } else {
                // 当前块已满，保存
                if (currentChunk.length() > tableStart.length()) {
                    currentChunk.append("</table>");
                    chunks.add(currentChunk.toString());
                }

                // 开始新块
                currentChunk = new StringBuilder();
                currentChunk.append(tableStart);

                // 检查单行是否超过限制
                String singleRowContent = tableStart + row + "</table>";
                if (singleRowContent.length() > maxSize) {
                    // 单行太大，拆分单元格
                    List<String> rowChunks = splitSingleTableRow(row, maxSize, tableStart);
                    chunks.addAll(rowChunks);
                    // 重置currentChunk
                    currentChunk = new StringBuilder();
                    currentChunk.append(tableStart);
                } else {
                    currentChunk.append(row);
                }
            }
        }

        // 保存最后一块
        if (currentChunk.length() > tableStart.length()) {
            currentChunk.append("</table>");
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * 统计子串出现次数
     */
    private static int countSubstring(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * 用简单的 </tr> 分割方式处理超大 HTML 表格，避免正则回溯
     */
    private static List<String> splitHtmlTableBySimpleTr(String content, int maxSize) {
        List<String> chunks = new ArrayList<>();

        // 提取 <table ...> 开头
        int tableTagEnd = content.indexOf('>');
        String tableStart = tableTagEnd > 0 ? content.substring(0, tableTagEnd + 1) : "<table>";
        List<String> rows = getStrings(content, tableTagEnd);

        // 按 maxSize 分组拼接
        StringBuilder currentChunk = new StringBuilder(tableStart);
        int headerLen = tableStart.length() + 8; // 预留 </table>
        for (String row : rows) {
            if (currentChunk.length() + row.length() + headerLen <= maxSize) {
                currentChunk.append(row);
            } else {
                if (currentChunk.length() > tableStart.length()) {
                    currentChunk.append("</table>");
                    chunks.add(currentChunk.toString());
                }
                currentChunk = new StringBuilder(tableStart);
                // 如果单行就超了，强制按字符拆分
                if (tableStart.length() + row.length() + 8 > maxSize) {
                    chunks.add(tableStart + row + "</table>");
                    continue;
                }
                currentChunk.append(row);
            }
        }
        if (currentChunk.length() > tableStart.length()) {
            currentChunk.append("</table>");
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    private static @NonNull List<String> getStrings(String content, int tableTagEnd) {
        String innerContent = tableTagEnd > 0 ? content.substring(tableTagEnd + 1) : content;
        if (innerContent.endsWith("</table>")) {
            innerContent = innerContent.substring(0, innerContent.length() - 8);
        }

        // 按 </tr> 简单切割
        String[] parts = innerContent.split("</tr>");
        List<String> rows = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            String row = trimmed + "</tr>";
            if (!row.startsWith("<tr") && row.contains("<tr")) {
                row = row.substring(row.indexOf("<tr"));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 拆分单行表格（按单元格拆分）
     * 注意：超大行可能触发正则回溯，需要复杂度保护
     */
    private static List<String> splitSingleTableRow(String row, int maxSize, String tableStart) {
        List<String> chunks = new ArrayList<>();

        // 如果行中 </td> 出现超过 30 次，跳过正则直接按字符拆分，避免回溯爆炸
        if (countSubstring(row, "</td>") > 30) {
            String fullContent = tableStart + row + "</table>";
            return forceSplitByChar(fullContent, maxSize, 0);
        }

        // 提取tr开始标签
        String trStart = "<tr>";
        Pattern trStartPattern = Pattern.compile("<tr[^>]*>");
        Matcher trStartMatcher = trStartPattern.matcher(row);
        int trStartEnd = 0;
        if (trStartMatcher.find()) {
            trStart = trStartMatcher.group();
            trStartEnd = trStartMatcher.end();
        }

        // 提取行内内容
        String rowInner = row;
        if (trStartEnd > 0) {
            rowInner = row.substring(trStartEnd);
            if (rowInner.endsWith("</tr>")) {
                rowInner = rowInner.substring(0, rowInner.length() - 5);
            }
        }

        // 提取单元格，使用简单字符串匹配代替正则，避免大表格行回溯
        List<String> cells = new ArrayList<>();
        int cellSearchPos = 0;
        while (cellSearchPos < rowInner.length()) {
            int tdStart = rowInner.indexOf("<td", cellSearchPos);
            if (tdStart == -1) break;
            int tdEnd = rowInner.indexOf("</td>", tdStart);
            if (tdEnd == -1) break;
            tdEnd += 5; // 包含 </td>
            String cell = rowInner.substring(tdStart, tdEnd);
            if (cell.trim().startsWith("<td")) {
                cells.add(cell);
            }
            cellSearchPos = tdEnd;
        }

        if (cells.isEmpty()) {
            // 没找到单元格，强制拆分
            String fullContent = tableStart + row + "</table>";
            return forceSplitByChar(fullContent, maxSize, 0);
        }

        StringBuilder currentChunk = new StringBuilder();
        currentChunk.append(tableStart).append(trStart);

        int effectiveMaxSize = maxSize - 30; // 预留标签空间

        for (String cell : cells) {
            String potentialChunk = currentChunk + cell;

            if (potentialChunk.length() + 13 <= effectiveMaxSize) {
                currentChunk.append(cell);
            } else {
                if (currentChunk.length() > (tableStart + trStart).length()) {
                    currentChunk.append("</tr></table>");
                    chunks.add(currentChunk.toString());
                }

                currentChunk = new StringBuilder();
                currentChunk.append(tableStart).append(trStart);

                // 检查单个单元格是否超过限制
                String singleCellContent = tableStart + trStart + cell + "</tr></table>";
                if (singleCellContent.length() > maxSize) {
                    // 单个单元格太大，直接强制拆分
                    List<String> forcedChunks = forceSplitByChar(singleCellContent, maxSize, 0);
                    chunks.addAll(forcedChunks);
                } else {
                    currentChunk.append(cell);
                }
            }
        }

        if (currentChunk.length() > (tableStart + trStart).length()) {
            currentChunk.append("</tr></table>");
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * 拆分Markdown表格内容。
     * 使用逐行遍历避免大文本 split("\\n") 导致的内存峰值。
     */
    private static List<String> splitMarkdownTableContent(String content, int maxSize) {
        List<String> chunks = new ArrayList<>();

        if (content.length() <= maxSize) {
            chunks.add(content);
            return chunks;
        }

        List<String> lines = splitLines(content);
        StringBuilder currentChunk = new StringBuilder();

        // 提取表头（前两行通常是表头和分隔符）
        int headerEndIndex = Math.min(2, lines.size());
        StringBuilder tableHeader = new StringBuilder();
        for (int i = 0; i < headerEndIndex; i++) {
            if (i > 0) tableHeader.append("\n");
            tableHeader.append(lines.get(i));
        }

        currentChunk.append(tableHeader);

        for (int i = headerEndIndex; i < lines.size(); i++) {
            String line = lines.get(i);
            String potentialChunk = currentChunk.toString();
            if (!currentChunk.isEmpty() && !currentChunk.toString().endsWith("\n")) {
                potentialChunk += "\n";
            }
            potentialChunk += line;

            if (potentialChunk.length() <= maxSize) {
                if (!currentChunk.isEmpty() && !currentChunk.toString().endsWith("\n")) {
                    currentChunk.append("\n");
                }
                currentChunk.append(line);
            } else {
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString());
                }

                // 新块保留表头
                currentChunk = new StringBuilder();
                currentChunk.append(tableHeader).append("\n").append(line);
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * 逐行拆分文本，避免大文本 split("\\n") 造成内存峰值。
     */
    private static List<String> splitLines(String text) {
        List<String> lines = new ArrayList<>();
        int len = text.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (text.charAt(i) == '\n') {
                lines.add(text.substring(start, i));
                start = i + 1;
            }
        }
        // 最后一行（可能不以 \n 结尾）
        if (start < len) {
            lines.add(text.substring(start));
        } else if (start == len && len > 0 && text.charAt(len - 1) == '\n') {
            // 以 \n 结尾，添加空行
            lines.add("");
        }

        return lines;
    }

    /**
     * 强制按字符拆分（当句子本身超过限制时）
     */
    private static List<String> forceSplitByChar(String text, int maxSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + maxSize, text.length());

            // 尝试在合适的位置断开（逗号、空格、换行等）
            if (end < text.length()) {
                int breakPoint = findBreakPoint(text, start, end);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }

            chunks.add(text.substring(start, end));

            // 已经到达文本末尾，无需再重叠，直接退出
            if (end >= text.length()) {
                break;
            }

            int nextStart = end - overlapSize;

            // 确保重叠后仍有进展，否则从当前 end 继续（防止无限循环）
            if (nextStart <= start) {
                nextStart = end;
            }

            start = nextStart;
        }

        return chunks;
    }

    /**
     * 寻找合适的断点
     */
    private static int findBreakPoint(String text, int start, int end) {
        // 从后往前找合适的断点
        for (int i = end; i > start; i--) {
            char c = text.charAt(i);
            // 在逗号、分号、空格、换行、HTML标签结束处断开
            if (c == '，' || c == '、' || c == '；' || c == ' ' || c == '\n' || c == '。' || c == '>') {
                return i + 1;
            }
        }
        return end;
    }

    /**
     * 按句子拆分文本（兼容 \r\n 和 \n）。
     * 使用遍历方式避免大正则 split 导致的内存峰值。
     */
    private static List<String> splitBySentences(String text) {
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        List<String> sentences = new ArrayList<>();

        int len = normalized.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            char c = normalized.charAt(i);
            // 句子结束标记：。！？!?.
            if (c == '。' || c == '！' || c == '？' || c == '!' || c == '?' || c == '.') {
                // 跳过分隔符后的空白符
                int end = i + 1;
                while (end < len && (normalized.charAt(end) == ' ' || normalized.charAt(end) == '\n')) {
                    end++;
                }
                String sentence = normalized.substring(start, end).trim();
                if (!sentence.isEmpty()) {
                    sentences.add(sentence);
                }
                start = end;
                i = end - 1; // for 循环会 i++
            }
        }

        // 处理最后一部分（可能没有句子结束符）
        if (start < len) {
            String sentence = normalized.substring(start).trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }

        return sentences;
    }

    /**
     * 提取重叠内容（用于上下文衔接）
     */
    private static String extractOverlap(String text, int overlapSize) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (text.length() <= overlapSize) {
            return text;
        }

        // 从末尾提取指定长度的内容
        String overlap = text.substring(text.length() - overlapSize);

        // 尝试在句子边界处调整
        int firstSentenceEnd = overlap.indexOf('。');
        if (firstSentenceEnd > 0) {
            overlap = overlap.substring(firstSentenceEnd + 1);
        }

        return overlap;
    }

    /**
     * 创建Document对象
     */
    private static Document createDocument(RawChunk rawChunk, Map<String, Object> baseMetadata, int index) {
        String fullContent = "# " + rawChunk.headerPath + "\n\n" + rawChunk.content;

        Map<String, Object> docMetadata = new HashMap<>(baseMetadata);
        docMetadata.put("chunk_index", index);
        docMetadata.put("chunk_key", DigestUtils.md5Hex(fullContent));
        docMetadata.put("header_path", rawChunk.headerPath);
        docMetadata.put("chunk_size", fullContent.length());

        return new Document(fullContent.trim(), docMetadata);
    }

    // ==================== 文本预处理 ====================

    /**
     * 移除 HTML 表格中无实际内容的空行。
     * 例如 `<tr><td></td><td></td>...</tr>` 这类纯占位符行没有任何向量化价值，
     * 保留它们只会浪费内存和存储空间。
     * <p>
     * 只删除所有 <td> 标签内部去标签后内容为空（纯空白或空）的 <tr> 行。
     * 表头行（含 <th> 的）或含有任何文本内容的行不会删除。
     */
    static String removeEmptyTableRows(String text) {
        // 先统一换行符
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");

        // 没有表格标签，直接返回
        if (!normalized.contains("</tr>")) {
            return text;
        }

        // 如果 </tr> 太多（超大表格），走简单字符串切割方式
        if (countSubstring(normalized, "</tr>") > 100) {
            return removeEmptyTableRowsSimple(normalized);
        }

        // 匹配完整 <tr>...</tr>，用 DOTALL 确保跨行匹配
        Pattern trPattern = Pattern.compile("<tr[^>]*>(.*?)</tr>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = trPattern.matcher(normalized);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String fullTr = matcher.group();
            String inner = matcher.group(1);

            // 只处理不含 <th> 且内容区纯空的 <tr>（删除所有 HTML 标签后空白即为空）
            if (!containsTag(inner) && isBlankAfterStripTags(inner)) {
                matcher.appendReplacement(result, "");
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(fullTr));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 简单字符串切割方式移除空表格行，避免大文本上的正则回溯
     */
    private static String removeEmptyTableRowsSimple(String text) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        while (pos < text.length()) {
            int trStart = text.indexOf("<tr", pos);
            if (trStart == -1) {
                result.append(text.substring(pos));
                break;
            }
            int trEnd = text.indexOf("</tr>", trStart);
            if (trEnd == -1) {
                result.append(text.substring(pos));
                break;
            }
            trEnd += 5; // 包含 </tr>

            // 输出 <tr 之前的部分
            result.append(text, pos, trStart);

            String fullTr = text.substring(trStart, trEnd);
            // 提取 <tr...> 和 </tr> 之间的内容
            int innerStart = text.indexOf('>', trStart);
            // 如果找不到 '>' 或 '>' 位置已经在 </tr> 之后（畸形标签），直接保留整行
            if (innerStart == -1 || innerStart >= trEnd - 5) {
                result.append(fullTr);
                pos = trEnd;
                continue;
            }
            innerStart += 1; // 跳过 '>'
            String inner = text.substring(innerStart, trEnd - 5);

            // 含 <th> 或去掉标签后非空 → 保留，否则删除
            if (text.substring(trStart, innerStart).toLowerCase().contains("<th")
                    || inner.toLowerCase().contains("<th")
                    || !isBlankAfterStripTags(inner)) {
                result.append(fullTr);
            }

            pos = trEnd;
        }
        return result.toString();
    }

    /**
     * 去掉所有 HTML 标签后，剩余内容是否纯空白。
     * 使用简单遍历代替 replaceAll 正则，避免大文本回溯。
     */
    private static boolean isBlankAfterStripTags(String html) {
        boolean inTag = false;
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '<') {
                inTag = true;
            } else if (c == '>') {
                inTag = false;
            } else if (!inTag && c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return false; // 发现非空白字符
            }
        }
        return true;
    }

    /**
     * 简单判断是否包含 th 标签
     */
    private static boolean containsTag(String html) {
        // 使用 toLowerCase + indexOf 代替正则，避免不必要的 Pattern 编译
        String lower = html.toLowerCase();
        int idx = lower.indexOf("<th");
        if (idx == -1) return false;
        // 确保后面是 > 或空格（真正的标签）
        return idx + 3 < lower.length() && (lower.charAt(idx + 3) == '>' || lower.charAt(idx + 3) == ' ');
    }

    // ==================== 便捷方法 ====================

    /**
     * 使用默认设置拆分
     */
    public static List<Document> split(String markdownText, Map<String, Object> metadata) {
        return splitByHeadersWithHierarchy(markdownText, metadata);
    }

    /**
     * 指定最大块大小拆分
     */
    public static List<Document> split(String markdownText, Map<String, Object> metadata, int maxChunkSize) {
        return splitByHeadersWithHierarchy(markdownText, metadata, maxChunkSize);
    }
}