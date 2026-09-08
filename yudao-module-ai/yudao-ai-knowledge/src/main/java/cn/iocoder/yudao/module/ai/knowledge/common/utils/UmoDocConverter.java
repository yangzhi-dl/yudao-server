package cn.iocoder.yudao.module.ai.knowledge.common.utils;

import cn.hutool.core.util.StrUtil;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UMO Doc 格式与 Markdown 的双向转换器。
 * <p>
 * Markdown → UMO HTML：使用 flexmark 解析 Markdown 并转为 HTML，
 * 图片会转换为 {@code <figure id="{fileId}" data-type="image">} 格式。
 * <p>
 * UMO HTML → Markdown：使用 flexmark {@link FlexmarkHtmlConverter} 将 HTML 还原为 Markdown，
 * figure 媒体节点中的 id 通过 alt 文本保留。
 *
 */
@Slf4j
public final class UmoDocConverter {

    /**
     * 匹配 alt 文本中的文件 ID 格式：纯数字 + .png 后缀
     * 例如 "12345.png" → fileId = 12345
     */
    private static final Pattern IMAGE_ID_PATTERN = Pattern.compile("^(\\d+)\\.png$");

    // ========== Markdown → HTML 组件 ==========

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, Arrays.asList(
                        TablesExtension.create(),
                        StrikethroughExtension.create(),
                        TaskListExtension.create(),
                        AutolinkExtension.create()
                ))
                .set(HtmlRenderer.SOFT_BREAK, " ")
                .set(HtmlRenderer.ESCAPE_HTML, false)
                .set(HtmlRenderer.SUPPRESS_HTML_BLOCKS, false)
                .set(HtmlRenderer.RENDER_HEADER_ID, false)
                .set(HtmlRenderer.GENERATE_HEADER_ID, false);

        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    // ========== HTML → Markdown 组件 ==========

    private static final FlexmarkHtmlConverter HTML_TO_MARKDOWN_CONVERTER =
            FlexmarkHtmlConverter.builder().build();

    // ========================== Markdown → UMO HTML ==========================

    /**
     * 将 Markdown 内容转换为 UMO Doc 格式的 HTML。
     *
     * @param markdown Markdown 内容
     * @return UMO 兼容的 body HTML 字符串。输入为空时返回空字符串
     */
    public static String convert(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return "";
        }

        try {
            String htmlBody = RENDERER.render(PARSER.parse(markdown));
            return postProcessForUmo(htmlBody);
        } catch (Exception e) {
            log.error("Markdown 转 UMO HTML 失败，返回降级内容", e);
            return fallback(markdown);
        }
    }

    /**
     * 对 flexmark 输出的 HTML 进行后处理，使其符合 UMO 格式要求。
     * <p>
     * 主要处理：
     * <ul>
     *   <li>将 {@code <img>} 转换为 {@code <figure id="{fileId}">} 格式，
     *       使 {@code resolveContentFileUrls()} 能通过 figure[id] 刷新签名 URL</li>
     * </ul>
     */
    private static String postProcessForUmo(String html) {
        Document doc = Jsoup.parseBodyFragment(html);
        boolean modified = false;

        for (Element img : doc.select("img")) {
            String alt = img.attr("alt");
            Matcher matcher = IMAGE_ID_PATTERN.matcher(alt);
            if (!matcher.matches()) {
                continue;
            }

            String fileId = matcher.group(1);
            String src = img.attr("src");
            String title = img.attr("title");

            // 使用 title 作为上下文描述
            String context = StringUtils.isNotBlank(title) ? title : "";

            // 构建 figure 节点
            Element figure = doc.createElement("figure");
            figure.attr("id", fileId);
            figure.attr("data-type", "image");
            figure.attr("name", alt);
            figure.attr("style", "justify-content: center;");

            // 构建 img（不带 id，id 放在 figure 上供 resolveContentFileUrls 解析）
            Element newImg = doc.createElement("img");
            newImg.attr("src", src);
            newImg.attr("alt", context);

            figure.appendChild(newImg);
            img.replaceWith(figure);
            modified = true;
        }

        return modified ? doc.body().html() : html;
    }

    /**
     * 降级处理：转换失败时将 markdown 原文以 pre 文本形式返回，前端可展示原始内容。
     */
    private static String fallback(String markdown) {
        String escaped = markdown
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<pre style=\"white-space:pre-wrap;word-break:break-word;\">" + escaped + "</pre>";
    }

    // ========================== UMO HTML → Markdown ==========================

    /**
     * 将 UMO Doc 格式的 HTML 内容转换为 Markdown。
     * <p>
     * 处理 UMO 编辑器保存的 figure 媒体节点（id 在 figure 上），
     * 以及普通 img/video/audio 节点（id 在元素自身上），
     * 将媒体信息转换为 Markdown 图片语法，id 保留在 alt 文本中。
     * <p>
     * HTML → Markdown 底层使用 flexmark {@link FlexmarkHtmlConverter}。
     *
     * @param umoHtml UMO HTML 内容
     * @return Markdown 字符串。输入为空时返回空字符串
     */
    public static String convertToMarkdown(String umoHtml) {
        if (StringUtils.isBlank(umoHtml)) {
            return "";
        }

        try {
            // 第 1 步：将 figure/media 节点预处理为 <img> 形式，id 编码到 alt 中
            String preprocessed = preprocessMediaForMarkdown(umoHtml);

            // 第 2 步：使用 flexmark 将 HTML 转为 Markdown
            return HTML_TO_MARKDOWN_CONVERTER.convert(preprocessed).trim();
        } catch (Exception e) {
            log.error("UMO HTML 转 Markdown 失败，返回原始内容", e);
            return umoHtml;
        }
    }

    /**
     * 预处理 HTML 中的媒体元素，将 id 信息编码到 alt 文本中，
     * 统一转为 {@code <img alt="{id}.{ext}">} 格式，便于后续 HTML→Markdown 转换。
     * <p>
     * 处理两种节点形态：
     * <ul>
     *   <li>{@code <figure id="123" data-type="image"><img src="url"></figure>}
     *       → {@code <img src="url" alt="123.png">}</li>
     *   <li>{@code <figure id="123" data-type="video"><video src="url"></video></figure>}
     *       → {@code <img src="url" alt="123.mp4">}</li>
     *   <li>{@code <img id="123" src="url">} (直接带 id)
     *       → {@code <img src="url" alt="123.png">}</li>
     *   <li>{@code <video id="123" src="url">}
     *       → {@code <img src="url" alt="123.mp4">}</li>
     * </ul>
     */
    private static String preprocessMediaForMarkdown(String html) {
        Document doc = Jsoup.parseBodyFragment(html);
        boolean modified = false;

        // 处理 figure 包裹的媒体元素（UMO 编辑器标准格式，id 在 figure 上）
        for (Element figure : doc.select("figure[id]")) {
            String fileId = figure.attr("id");
            Element media = figure.selectFirst("img, video, audio");
            if (media == null) {
                continue;
            }

            Element img = doc.createElement("img");
            img.attr("src", fileId);
            img.attr("alt", figure.attr("name"));
            figure.replaceWith(img);
            modified = true;
        }

        // 处理带 id 的独立媒体元素
        for (Element media : doc.select("img[id], video[id], audio[id]")) {
            String fileId = media.attr("id");

            Element img = doc.createElement("img");
            img.attr("src", fileId);
            img.attr("alt", media.attr("alt"));
            media.replaceWith(img);
            modified = true;
        }

        return modified ? doc.body().html() : html;
    }

    // ========================== 签名 URL 刷新 ==========================

    /**
     * 解析 HTML 内容中的文件 ID 属性，替换为新的预签名 URL。
     * <p>
     * 编辑器保存的媒体节点格式：
     * {@code <img src="旧签名URL" id="12345">} 或 {@code <video src="..." id="12345">}
     * <p>
     * 该方法识别 figure 包裹的 img/video/audio，通过 figure[id]
     * 查找文件并调用 {@code urlProvider} 生成新的预签名 URL。
     *
     * @param content     原始 HTML 内容
     * @param urlProvider 根据 fileId 生成签名 URL 的函数
     * @return 替换了签名 URL 后的内容
     */
    public static String resolveContentFileUrls(String content, Function<Long, String> urlProvider) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        Document doc = Jsoup.parse(content);
        boolean modified = false;

        for (Element figure : doc.select("figure[id]")) {
            Long fileId = parseFileId(figure.attr("id"));
            if (fileId == null) {
                continue;
            }
            String signedUrl = urlProvider.apply(fileId);
            if (StrUtil.isBlank(signedUrl)) {
                continue;
            }
            Element media = figure.selectFirst("img, video, audio");
            if (media != null) {
                media.attr("src", signedUrl);
                modified = true;
            }
        }

        return modified ? doc.body().html() : content;
    }

    /**
     * 尝试将 id 属性值解析为文件 ID，非数字的 id（如编辑器生成的随机 id）会被忽略。
     */
    public static Long parseFileId(String idAttr) {
        if (StrUtil.isBlank(idAttr)) {
            return null;
        }
        try {
            return Long.parseLong(idAttr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 提取 HTML 内容中引用的资源文件 ID 列表（去重、保序）。
     * <p>
     * 识别三种形态：
     * <ul>
     *   <li>{@code <figure id="{fileId}" data-type="image|video|audio">}（UMO 标准媒体节点）</li>
     *   <li>{@code <img/video/audio id="{fileId}">}（直接带 id 的媒体元素）</li>
     *   <li>{@code <img data-id="{fileId}">}（umo 编辑器上传节点 / chat Markdown 渲染形态）</li>
     * </ul>
     * 供保存文档内容时更新 {@code ai_document.resource_ids}，
     * 使正文中的资源文件可通过所属文档做访问权限判定。
     *
     * @param content 文章 HTML 内容
     * @return 资源文件 ID 列表（无资源时返回空列表）
     */
    public static List<Long> extractResourceIds(String content) {
        if (StrUtil.isBlank(content)) {
            return List.of();
        }
        Document doc = Jsoup.parse(content);
        Set<Long> ids = new LinkedHashSet<>();
        // 1) figure 包裹的 UMO 媒体节点：id 在 figure 上
        for (Element figure : doc.select("figure[id]")) {
            Long fileId = parseFileId(figure.attr("id"));
            if (fileId != null) {
                ids.add(fileId);
            }
        }
        // 2) 直接带 id 的媒体元素
        for (Element media : doc.select("img[id], video[id], audio[id], a[id]")) {
            Long fileId = parseFileId(media.attr("id"));
            if (fileId != null) {
                ids.add(fileId);
            }
        }
        // 3) data-id 形态（umo 上传节点 / Markdown 渲染）
        for (Element media : doc.select("[data-id]")) {
            Long fileId = parseFileId(media.attr("data-id"));
            if (fileId != null) {
                ids.add(fileId);
            }
        }
        return new ArrayList<>(ids);
    }

}
