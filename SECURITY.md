# 安全说明 (Security Policy)

## 报告漏洞

如果你发现安全漏洞，请在 GitHub 上提交 Issue（本项目独立开发者维护，无需私密报告流程）。

## 开发安全规范

### 禁止提交以下文件

- `local.properties` — Android SDK 路径
- `*.jks` / `*.keystore` — 签名密钥
- `*.p12` / `*.pfx` — 证书文件
- `keystore.properties` / `signing.properties` — 签名配置
- `secrets.properties` — 任何密钥配置
- `google-services.json` — Firebase 配置
- `.env` / `.env.*` / `.envrc` — 环境变量
- `*_credentials.json` — 凭证文件

以上所有文件已在 `.gitignore` 中排除。

### 密钥和凭证

- **永远不要把真实 API key、token、密码、签名文件写入源码。**
- 如果将来需要示例配置，只能创建 `.example` 文件（如 `secrets.properties.example`），并在文件中使用占位符。
- 所有真实配置必须放在本地未追踪文件中。

### 第三方 SDK

- 当前版本不使用任何第三方数据 SDK（Analytics、Crashlytics 等）。
- 如果将来引入需要 API key 的 SDK，必须通过 `local.properties` 注入密钥，不能硬编码。

### GitHub Actions

- 修改 CI/CD 配置前，必须说明是否会接触 secrets。
- Secrets 必须通过 GitHub Secrets 管理，不能写入工作流文件。
- 当前项目无 CI/CD 配置。

### 客户端安全说明

如果将来添加联网或 API 功能，需要注意：
- **客户端无法保护服务端密钥**——任何硬编码在 APK 中的 key 都可以被反编译提取。
- 需要 API 访问的场景应考虑代理模式（客户端 → 你的服务端 → 第三方 API）。

## 依赖项

当前依赖全部来自 Google/Android 官方：
- `androidx.*` — Android Jetpack
- `com.google.devtools.ksp` — Kotlin Symbol Processing
- `org.jetbrains.kotlin` — Kotlin
