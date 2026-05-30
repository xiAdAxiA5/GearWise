# GearWise - Claude Code 项目规则

## 安全规则

### 密钥和凭证
- **永远不要把真实 API key、token、密码、签名文件写入源码。**
- 如果需要示例配置，只能创建 `.example` 文件（如 `keystore.properties.example`），并使用占位符值。
- 所有真实配置必须放在 `.gitignore` 已排除的本地文件中。

### Git 提交
- 不提交 `local.properties`、`.env`、`*.jks`、`*.keystore`、`keystore.properties`
- 不提交 `google-services.json` 或任何 Firebase/云服务配置文件
- 修改 `.gitignore` 前确认敏感文件仍被覆盖

### CI/CD
- 修改 GitHub Actions 前必须说明是否会接触 secrets
- Secrets 必须通过 GitHub Secrets 管理，不能写入工作流文件

### 联网/API 功能
- 如果用户要求加入联网或 API 功能，必须先解释：**客户端不能保护服务端密钥** — 硬编码在 APK 中的任何 key 都可以被反编译提取
- 需要外部 API 时优先考虑代理模式（客户端 → 后端 → 第三方 API）

## 技术约束
- 本项目是完全本地化的 Android 应用
- 不使用任何网络请求库（无 OkHttp、Retrofit）
- 不在 AndroidManifest 中声明 INTERNET 权限
- 数据仅存储在设备本地 Room 数据库中
- 不接入任何第三方分析/埋点/广告 SDK
