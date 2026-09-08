# Dzint Nexus 平台（dzint-agent-server）

一套面向制造业的 **多租户 SaaS 智能体平台**：以 AI Agent 能力为核心引擎，向下打通 MES（制造执行）、WMS（仓储管理）、BPM（流程审批）、CRM/ERP 等业务域，向上通过 Hub 前台和运营分析（Analytics）实现商业化交付。

> 基座：芋道（yudao-boot）单体多模块脚手架，版本 `2026.06-jdk25-SNAPSHOT`。架构细节与演进全景详见 [`doc/项目全景介绍.md`](doc/项目全景介绍.md)。

| 事实 | 说明 |
|------|------|
| 服务名 | `dzint-agent-server`（配置于 `application.yaml`） |
| 产品线 | Nexus（生产库 `dzint-nexus-pro`） |
| 运行形态 | 单体部署、模块化组装：`yudao-server` 按需聚合各业务模块 |
| 语言 / 运行时 | Java 25 |
| 框架 | Spring Boot 4.1（Jakarta 生态） |
| 分层 | 底座层（多租户 / RBAC / 工作流 / 基础设施）+ 业务层（MES / WMS / CRM / ERP / BPM）+ 智能层（AI / Analytics / Hub） |

---

## 技术栈

| 维度 | 选型 |
|------|------|
| 框架 | Spring Boot 4.1、Maven（flatten 统一 revision 版本） |
| ORM | MyBatis-Plus + MyBatis-Plus-Join + 动态多数据源 + Druid |
| 缓存 | Spring Data Redis |
| 工作流 | Flowable |
| 消息 | Kafka（含 `kafka-lab` 消费治理实验） |
| 数据库 | MySQL 为主，SQL 脚本覆盖 MySQL / PostgreSQL / Oracle / SQLServer / DM / KingBase / HighGo / OpenGauss / DB2 |
| 其它 | LiveKit（语音/视频 Agent PoC）、Milvus 向量库（AI 知识库检索）、LibreOffice（Office 转换，按需启用） |

## 仓库结构

```
.
├── pom.xml                    # Maven 聚合根
├── sql/                       # 各数据库建库脚本（mysql / oracle / pg / dm / kingbase ...）
├── doc/                       # 项目全景介绍、数据存储层开发手册
├── yudao-dependencies         # BOM：全工程依赖版本统一管理
├── yudao-framework            # 15 个自研 Spring Boot Starter（web/security/mybatis/redis/mq/job/excel/websocket/monitor + SaaS 三件套）
├── yudao-server               # 启动器：聚合装配所有启用的模块
├── yudao-module-system        # 租户 / 用户 / 角色 / 部门 / 字典（SaaS 底座）
├── yudao-module-infra         # 代码生成、文件、定时任务、监控
├── yudao-module-bpm           # Flowable 审批流
├── yudao-module-crm / erp     # 标准 CRM / ERP
├── yudao-module-mes           # ★ 制造执行系统（最重的自研业务资产）
├── yudao-module-wms           # ★ 独立仓储管理（开发中）
├── yudao-module-ai            # ★ AI 模块（common/core/data/knowledge/search 等 6 子模块）
├── yudao-module-analytics     # ★ 平台运营分析（跨租户全局统计）
├── yudao-module-hub           # ★ 用户前台与商业交付聚合模块（骨架）
├── yudao-module-kafka-lab     # Kafka IM 消费稳定性复现实验（见其 README）
└── yudao-ui/
    ├── yudao-ui-admin-vue3    # 管理后台前端（Vue3，主用）
    ├── yudao-ui-admin-vben    # 管理后台前端（Vben 风格）
    ├── yudao-ui-admin-vue2    # 管理后台前端（Vue2）
    ├── yudao-ui-admin-uniapp  # 管理端 / 员工端 uniapp
    └── yudao-ui-mall-uniapp   # 商城 uniapp
```

带 ★ 的五个模块为自研增量（其中 `hub` 目前仅骨架）。

## 各模块现状

| 模块 | 规模 | 成熟度 | 说明 |
|------|------|--------|------|
| system / infra | 芋道基座 | 成熟 | SaaS 租户、权限、代码生成、文件、任务 |
| bpm / crm / erp | 芋道基座 | 成熟 | 按需启用 |
| **mes** | 369 个 Java 类 | 可用，业务侧最完整 | 八大域：md 主数据 / cal 排程 / pro 生产 / qc 质量 / wm 仓储 / dv 设备 / tm 工装 / home 看板 |
| **wms** | 58 个 Java 类 | 开发中 | 库存域已闭环，四类单据（入库/出库/移库/盘点）Service 层已实现，DAL/Controller 待补全 |
| **ai** | — | 重构中 | 6 子模块已立；本仓库暂含 pom / Mapper XML 等资源，Java 源码在同步链路上 |
| **analytics** | 10 | 可用 | 跨租户运营统计（用户/租户/AI 用量/Token 消耗等） |
| **hub** | 0 | 骨架 | dependencies/common/core 已立，等待填充 |

### 核心链路（简）

- **MES 生产闭环**：主数据 → 工艺路线 → 工单 → 生产任务 → 派工 → 报工（事务内联动物料消耗、成品产出、产量回写）→ 质量检验 → 成品出入库；
- **WMS 库存链路**：四类单据共用状态机，完成动作统一收敛到 `changeInventory` / `checkInventory` 两个事务入口，保证账实一致；
- **AI 智能层**：大模型接入与对话、知识库 RAG（文档入库 → 切片 → 向量化 → 检索问答）、可视化 AI 工作流（快照式发布）、AI 数据资产管线、Mermaid 校验与 LiveKit PoC；
- **Analytics**：`@TenantIgnore` 平台级跨租户统计，与租户业务视角分离。

## 快速开始

### 环境要求

- JDK 25、Maven 3.9+
- MySQL 8.x、Redis（Kafka 仅个人实验环境启用）

### 1. 初始化数据库

以 MySQL 为例，执行 `sql/mysql/dzint-nexus-pro.sql` 完成建库与演示数据初始化。

> 注意：SQL 脚本中的云存储 / 短信渠道等配置为**演示占位符**（`your-xxx-key`），真实密钥请在系统后台自行配置，勿直接提交真实凭据。

### 2. 配置

- `yudao-server/src/main/resources/application.yaml` 为基线配置；
- 各环境配置（`application-local/dev/prod/personal...y*ml`）**不入库**（.gitignore 已忽略），需按本机/部署环境自行维护；
- 搜索服务密钥通过环境变量注入：`BAIDU_API_KEY`（百度千帆）、`TAVILY_API_KEY`（Tavily），未设置时对应服务不生效。

### 3. 启动后端

```bash
# 本地默认环境（local）：端口 48080
mvn -pl yudao-server -am spring-boot:run
```

- 管理端 API 前缀：`/admin-api`，接口文档：`http://127.0.0.1:48080/doc.html`（`application.yaml` 中开启 springdoc/knife4j 后可用）；
- 若使用 `application-personal.yml`（`-Dspring-boot.run.profiles=personal` 或 IDE 指定），端口为 18080。

### 4. 启动前端

```bash
cd yudao-ui/yudao-ui-admin-vue3
npm install
npm run dev
```

## 开发规范（数据层）

调用链统一为 `Controller → Service → Mapper(MySQL) / RedisDAO(Redis)`；DO 继承 `BaseDO`（自动填充 creator/updater/createTime/updateTime/deleted，逻辑删除全局拦截）；Mapper XML 放 `resources/mapper/{业务域}/`；金额统一 `BigDecimal`（单位元）。完整约定见 [`doc/数据存储层开发手册.md`](doc/数据存储层开发手册.md)。

## 提交与安全约定

- **严禁提交真实密钥**：数据库密码、云厂商 AccessKey、第三方 API Key 等一律不进版本库；配置文件中的密钥必须使用环境变量引用或占位符；
- `.gitignore` 已忽略 `application-*.y*ml`（本地/部署环境配置）及 `.workbuddy-ai/` 等本地目录；
- SQL 种子数据中如需云存储/短信示例行，密钥位一律使用占位符。

## 相关文档

- [`doc/项目全景介绍.md`](doc/项目全景介绍.md)——项目架构、模块成熟度、核心链路与演进规划（建议先读）
- [`doc/数据存储层开发手册.md`](doc/数据存储层开发手册.md)——数据层开发强约束
- [`yudao-module-kafka-lab/README.md`](yudao-module-kafka-lab/README.md)——Kafka 消费治理实验说明
