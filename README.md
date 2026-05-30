# GearWise - 电子产品记录工具

一个完全离线的 Android 应用，用于记录你购买过的电子产品，自动计算实际持有成本和日均/月均使用成本。

> 📱 记录你的每一台设备，了解你真正花了多少钱。

## 功能

- **记录电子产品**：手机、电脑、平板、耳机等
- **追踪完整成本**：购买价格 + 配件支出 + 维修支出 - 出售回血
- **自动计算**：持有天数、实际成本、日均成本、月均成本
- **完全离线**：所有数据保存在本地，无需网络，无需登录
- **Material You 设计**：支持 Dynamic Color，跟随系统主题

## 技术栈

| 层 | 技术 |
|---|---|
| UI | Jetpack Compose + Material3 |
| 数据库 | Room (本地 SQLite) |
| 架构 | MVVM (ViewModel + StateFlow) |
| 语言 | Kotlin 1.9 |
| 最低版本 | Android 8.0 (API 26) |

## 如何运行

1. 用 Android Studio (Hedgehog+) 打开本目录
2. 等待 Gradle 同步完成
3. 连接设备或启动模拟器，点击 Run

## 隐私

本应用**完全离线运行**，不上传任何数据到服务器。所有数据保存在你的设备本地数据库中。

详见 [PRIVACY.md](PRIVACY.md)

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request。在提交前请阅读 [SECURITY.md](SECURITY.md)。
