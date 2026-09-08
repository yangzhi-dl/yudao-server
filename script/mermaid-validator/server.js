/**
 * Mermaid 图表语法校验服务
 *
 * 基于 mermaid.js 官方 parse() API 进行语法校验。
 * 使用 jsdom 提供 Node.js 所需的 DOM 环境。
 *
 * 启动: node server.js
 * 端口: 3001 (可通过 PORT 环境变量修改)
 *
 * API:
 *   POST /api/validate
 *   Body: { "code": "flowchart TD\n  A --> B" }
 *
 *   成功响应: { "valid": true, "diagramType": "flowchart-v2" }
 *   失败响应: { "valid": false, "error": "Parse error message" }
 */

const { JSDOM } = require('jsdom');

// 创建 fake DOM 环境，供 mermaid 内部 DOMPurify 使用
const dom = new JSDOM('<!DOCTYPE html><html><body></body></html>', {
    url: 'http://localhost',
    runScripts: 'dangerously',
});
global.window = dom.window;
global.document = dom.window.document;
global.DOMPurify = require('dompurify')(dom.window);

const express = require('express');
const mermaid = require('mermaid').default;

const app = express();
app.use(express.json({ limit: '1mb' }));

mermaid.initialize({ startOnLoad: false });

// ==================== API ====================

/**
 * POST /api/validate
 * 校验 Mermaid 图表代码语法
 */
app.post('/api/validate', async (req, res) => {
    const { code } = req.body;

    if (!code || typeof code !== 'string' || code.trim().length === 0) {
        return res.json({ valid: false, error: '代码不能为空' });
    }

    try {
        const result = await mermaid.parse(code.trim());
        res.json({
            valid: true,
            diagramType: result.diagramType || 'unknown'
        });
    } catch (err) {
        res.json({
            valid: false,
            error: err.message || String(err)
        });
    }
});

/**
 * POST /api/detect-type
 * 检测 Mermaid 图表类型（不校验语法）
 */
app.post('/api/detect-type', async (req, res) => {
    const { code } = req.body;

    if (!code || typeof code !== 'string' || code.trim().length === 0) {
        return res.json({ valid: false, error: '代码不能为空' });
    }

    try {
        const type = await mermaid.detectType(code.trim());
        res.json({ valid: true, type });
    } catch (err) {
        res.json({ valid: false, error: err.message || String(err) });
    }
});

// ==================== 健康检查 ====================

app.get('/health', (_req, res) => {
    res.json({ status: 'ok', service: 'mermaid-validator' });
});

// ==================== 启动 ====================

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
    console.log('Mermaid Validator running on http://localhost:' + PORT);
    console.log('  POST /api/validate    - 校验 Mermaid 语法');
    console.log('  POST /api/detect-type - 检测图类型');
    console.log('  GET  /health          - 健康检查');
});
