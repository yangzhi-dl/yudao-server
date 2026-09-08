# Kafka IM 消费稳定性复现实验

本模块复用了旧 IM 项目的三张业务表和核心处理顺序，用于对比旧版消费方式与治理后的消费方式。

## 处理链路

```text
POST /admin-api/kafka-lab/publish
  -> KafkaLabProducer（Key = groupId）
  -> kafka-lab-im-raw（6 partitions）
     -> legacy: concurrency=1 + auto commit + 同步 800ms 慢任务 + 吞异常
     -> governed: concurrency=6 + DB 快路径 + 手动 ACK
        -> kafka-lab-im-slow
           -> concurrency=6 + 模拟 LLM + imbot_task upsert + 手动 ACK
  -> 重试 2 次仍失败
  -> 原 Topic + .DLT
  -> KafkaLabDltConsumer 记录人工补偿日志
```

消息落库依赖 `imbot_group_chat_logs.uk_message_uid` 去重；群状态依赖
`imbot_group.uk_platform_group` upsert；任务依赖
`imbot_task.uk_group_date_task` upsert。

## 运行治理版

在 `application-local.yaml` 中设置：

```yaml
yudao:
  kafka-lab:
    enabled: true
    legacy-enabled: false
    governed-enabled: true
```

重启服务后，携带正常的 yudao 登录凭证请求：

```http
POST /admin-api/kafka-lab/publish?scenario=SLOW_TASK&count=1000&groupPrefix=group&groupCount=60
GET  /admin-api/kafka-lab/statistics
```

## 运行旧版并复现堆积

把开关改成：

```yaml
legacy-enabled: true
governed-enabled: false
```

重启后发送同样的 1000 条 `SLOW_TASK`。旧版只有一个消费线程，并在该线程同步等待
800ms；理论上最大消费速率约为 1.25 条/秒，生产速度高于消费速度时 lag 持续增长。

不要同时开启两个模式；它们使用不同 Consumer Group，会各自完整消费一份消息，影响对比结果。

## 故障场景

- `FAST`：只写消息表与群表。
- `SLOW_TASK`：触发慢 Topic，最终 upsert `imbot_task`。
- `RETRYABLE`：前两次抛异常，第三次成功，验证固定间隔重试。
- `POISON`：不可重试异常，直接进入 `.DLT`。
- `PARTIAL_FAILURE`：旧版先写 DB 再失败且吞异常；治理版事务回滚并重试。
- `DUPLICATE`：一批消息共用 `message_uid`，验证唯一约束去重。

批量请求默认轮转 60 个 `groupId`，让消息分布到多个分区；同一群仍使用相同 Key，保持群内顺序。
如需验证任务表幂等，可重复调用
`POST /admin-api/kafka-lab/publish?scenario=SLOW_TASK&count=10&groupCount=1&messageUid=10001`。
