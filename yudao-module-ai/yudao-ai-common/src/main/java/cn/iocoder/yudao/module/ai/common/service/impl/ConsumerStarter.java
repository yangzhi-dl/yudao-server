package cn.iocoder.yudao.module.ai.common.service.impl;

import cn.iocoder.yudao.module.ai.common.service.MarkdownConversionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerStarter {

    private final MarkdownConversionService markdownConversionService;

    @Value("${iims.document.parser.consumer-count:3}")
    private int consumerCount;

    @PostConstruct
    public void startConsumers() {
        for (int i = 0; i < consumerCount; i++) {
            markdownConversionService.startConsumer();
            log.info("FileToMarkdown 消费者 #{} 已启动", i + 1);
        }
    }
}