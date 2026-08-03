# AppRecommendations 业务接入规则

本文用于指导新的 iOS App 接入 `AppRecommendations` 中的推荐 App 资源与 `BioScanKit` 共用业务。规则综合 iNature、Mr.Mushroom、Mr.Rock、NatureEar 的实际迁移结果。

## 1. 适用范围与目标

- 适用平台：iOS 16 及以上。
- 共享能力：推荐 App、设计主题、设置页、拍照与裁剪流程、Paywall。
- 默认视觉基准：iNature。
- 接入必须是渐进式迁移；每一块业务独立验证后再删除 App 内旧实现。
- 接入后的页面行为、购买权益、识别结果、历史数据和埋点不得退化。

## 2. 共享边界

### 必须放在 AppRecommendations 中

- 推荐 App 的配置、图标和复制脚本。
- 多个 App 使用的 SwiftUI 页面结构、视觉组件和导航样式。
- 通用相机、裁剪、图片方向归一化和预览裁切映射。
- 通用 Paywall 状态机、购买结果模型和额度账本能力。
- 不依赖具体 App 数据模型的单元测试。

### 必须保留在各 App 中

- RevenueCat API Key、产品 ID、历史产品 ID 和现有 `UserDefaults` key。
- 具体识别模型、识别服务、领域结果类型、历史记录和图片存储。
- App 自己的路由、结果页、详情页、Coach Mark 和埋点实现。
- 品牌文案、颜色、字体、Logo、吉祥物和复杂 Hero。
- App 专属功能，例如 Favorites、调试工具、声音识别或地理信息工具。

禁止在 `BioScanKit` 中引用 `RevenueCatService.shared`、`AppAnalytics`、App 的状态单例或 App 私有资源。此类依赖必须通过协议、配置、Binding、闭包或 View Slot 注入。

当一项修复影响两个及以上 App 时，优先在 `BioScanKit` 修复；只影响单个 App 的业务差异保留在该 App。不得通过判断 App 名称或 Bundle ID 在共享组件中制造分支。

## 3. 模块选择

### 3.1 iOS

按实际能力选择最小依赖，不要为了方便默认链接所有模块。

| 模块 | 使用场景 | App 仍需提供 |
| --- | --- | --- |
| `BioScanDesign` | 主题、颜色、通用导航样式 | 品牌 Theme 和专属素材 |
| `BioScanSettings` | 设置容器、设置行、推荐 App、历史入口 | 设置动作、业务页面、历史仓库 |
| `BioScanCapture` | 相机页面、裁剪、Processing、图片处理 | 相机驱动或 Preview、识别服务、结果路由 |
| `BioScanPaywall` | Paywall UI、Store、账本协议 | Billing Adapter、产品目录、权益和埋点 |
| `BioScanKit` | 确实同时需要全部模块的 App | 上述全部 App 侧能力 |

NatureEar 没有拍照识别业务，因此只接入 Design、Settings 和 Paywall，禁止为保持形式一致而引入 Capture。

### 3.2 Android

Android 原生 Compose 实现在 `BioScanKit/Android`，包名按能力分为：

- `com.bioscankit.android.design`
- `com.bioscankit.android.settings`
- `com.bioscankit.android.capture`
- `com.bioscankit.android.paywall`

Android 与 iOS 使用相同的功能边界和 iNature 默认配置，但不共享 UI 源码。CameraX 会话、Activity、Navigation、RevenueCat/Google Billing Adapter、产品 ID、识别模型、历史仓库和埋点继续留在宿主 App。

兄弟仓库接入示例：

```kotlin
// settings.gradle.kts
include(":bioscankit")
project(":bioscankit").projectDir = file("../../AppRecommendations/BioScanKit/Android")

// app/build.gradle.kts
dependencies {
    implementation(project(":bioscankit"))
}
```

Android 推荐 App 必须使用明确的 Google Play URL 字段；不得把 `appStoreURL` 当作 Android 跳转地址。没有对应平台 URL 的条目安全过滤为空。

## 4. 仓库与工程接入

### 4.1 目录约定

本地开发采用兄弟仓库布局：

```text
Develop/
├── AppRecommendations/
│   └── BioScanKit/
└── SomeApp/
    └── ios/
```

当 Xcode 工程根目录为 `SomeApp/ios` 时，本地 Package 路径为：

```yaml
packages:
  BioScanKit:
    path: ../../AppRecommendations/BioScanKit
```

- 不要把 `AppRecommendations` 作为 App 仓库的 Git submodule；现有 App 已迁移为兄弟仓库加 CI bootstrap。
- 使用 XcodeGen 的工程必须以 `project.yml` 为源文件修改并重新生成工程；不能只修改 `.pbxproj`。
- 非 XcodeGen 工程应在 Xcode Package Dependencies 中添加相同的本地路径，并提交 `.pbxproj` 变更。
- 修改 Package 公共 API 前，必须在工作区搜索全部调用方：

```sh
rg -n "BioScanKit|BioScanDesign|BioScanSettings|BioScanCapture|BioScanPaywall" \
  /path/to/workspace -g '*.swift' -g 'project.yml' -g '*.pbxproj'
```

### 4.2 Xcode Cloud bootstrap

Xcode Cloud 不会自动拥有兄弟仓库。App 的 `ci_post_clone.sh` 必须在解析 Package 前把共享仓库拉到主仓库同级目录：

```bash
#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PRIMARY_REPOSITORY="${CI_PRIMARY_REPOSITORY_PATH:-$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel)}"
SHARED_REPOSITORY="$(cd "$PRIMARY_REPOSITORY/.." && pwd)/AppRecommendations"

if [[ ! -d "$SHARED_REPOSITORY/BioScanKit" ]]; then
    git clone --depth 1 --branch main \
        https://github.com/benzhipeng/AppRecommendations.git \
        "$SHARED_REPOSITORY"
fi

PACKAGE_MANIFEST="$SHARED_REPOSITORY/BioScanKit/Package.swift"
if [[ ! -f "$PACKAGE_MANIFEST" ]]; then
    echo "error: BioScanKit package was not downloaded to $PACKAGE_MANIFEST" >&2
    exit 1
fi
```

- 脚本位置必须符合 Xcode Cloud 对当前仓库根目录的发现规则；仓库根目录调整后同步移动或增加薄转发脚本。
- bootstrap 必须可重复执行，并在 Package 缺失时明确失败，不能让错误延迟到 Xcode 的 Package 解析阶段。
- CI 默认跟随 `AppRecommendations/main`。发布期需要完全可复现时，应改为明确 tag 或 commit，并记录升级步骤。

## 5. 推荐 App 资源

`RecommendedApps.json`、`Icons/*.png` 和 `Scripts/copy_app_recommendations.sh` 是一个整体，必须同时接入。

### 5.1 Build Phase

App target 增加名为 `Copy App Recommendations` 的 post-build script：

```sh
set -eu
script="${SRCROOT}/../../AppRecommendations/Scripts/copy_app_recommendations.sh"
/bin/sh "$script"
```

- 开启 User Script Sandboxing 时，必须把 JSON、全部图标和脚本声明为 Input Files，把 App Bundle 中对应文件声明为 Output Files。
- 新增或删除图标时，同步修改所有 XcodeGen `inputFiles` / `outputFiles`；否则增量构建或 CI 可能使用旧资源。
- 构建后必须确认 `RecommendedApps.json` 和所有可见 App 图标已进入最终 App Bundle。

### 5.2 配置规则

- `id` 使用稳定的小写标识，并与 `SettingsConfiguration.currentAppID` 完全对应。
- 已上架 App 使用真实 App Store URL；未上架 App 使用 `isVisible: false`，不能用搜索页冒充正式上架链接并保持可见。
- 每项必须提供 `title`、`description`、`imageName`、`backgroundColors`、`appStoreURL` 和 `fallbackMessage`。
- 当前 App 必须被排除，隐藏项不得展示。
- 新代码统一使用 `RecommendedAppsLoader` 或 `SettingsScreen`，不要在 App 中再次定义一套 JSON Decoder 和推荐卡片。
- JSON 缺失、字段损坏或图标缺失时必须安全降级为空列表或占位图，不能阻断设置页打开。

## 6. Theme 与导航

- 每个 App 定义唯一的共享主题入口，例如 `RockBioScanStyle.theme` 或 `MushroomBioScanStyle.theme`，并在 Settings、Capture、Paywall 中复用。
- iNature 直接使用 `.iNature`；其他 App 通过 `BioScanTheme` 和必要的 Paywall Theme 表达品牌差异。
- 颜色和布局差异应进入 Theme，文案和功能开关进入 Configuration，复杂品牌视图进入 View Slot。
- 被 `NavigationStack` push 的页面统一使用 `.bioScanPushNavigation(title:)`，保留系统返回动作和侧滑返回手势。
- 只有 Modal 或 App 明确接管返回动作时才传自定义 `backAction`；普通 push 页面不得隐藏系统返回后自行模拟 dismiss。
- Navigation Title、Toolbar 和背景样式只配置一次，避免父子页面重复设置造成跳动或双返回按钮。

## 7. Settings 接入

- 优先使用 `SettingsScreen` 作为页面容器，通过 `SettingsConfiguration` 提供 App 名称、当前 App ID、Theme、文案、链接和显示开关。
- 所有外部行为通过 `SettingsActions` 注入，包括 Restore、系统设置、语言、隐私协议、用户协议、反馈、评分、推荐 App 跳转和埋点。
- Membership 卡片放在 `membership` Slot；历史、工具、Debug 等 App 专属区域放在 `extra` Slot。
- iPad 需要自定义两栏布局时，使用带 `SettingsPageMetrics` 的 `extra` Slot，不能复制整个 Settings 容器。
- 历史入口和清空动作使用 `SettingsLibrarySectionView`。当前共享组件只负责 History 和 Clear History；Favorites 属于 App 专属业务，必须放在 `extra` 中单独实现。
- 清空历史必须同时清理记录与缓存图片，并在 UI 上禁用空历史的清理动作。异步仓库通过 `Task` 桥接到清理闭包。
- Restore 的显示状态必须跟随当前 Lifetime/订阅权益；Restore 返回值需区分恢复成功与无可恢复权益。
- 埋点通过 `SettingsActions.track` 映射回 App 自己的 Analytics，不在 Package 内硬编码事件名。
- 迁移完成后删除 App 内重复的 Settings Row、Recommended App Decoder 和推荐卡片；若为了渐进迁移暂时保留，必须标注后续删除点。

## 8. Capture 与裁剪接入

- iNature、Mr.Mushroom、Mr.Rock 使用 `CameraPage` 统一页面骨架；App 可继续注入已有相机 Preview 和驱动。
- 相机权限、镜头切换、闪光灯、缩放、对焦、相册入口等行为通过 `CameraPageActions` 或相应 Client 注入。
- App 继续拥有识别模型、额度检查、识别 Task、结果类型、历史保存和结果页路由。
- 裁剪页使用 `ImageCropEditor`，识别等待页使用 `RecognitionProcessingScreen`，输入图片先经过 `BioScanImageProcessing.normalized`。
- 从 aspect-fill 相机预览映射到原始照片时，必须使用 `BioScanImageProcessing.cropAspectFillPreview` 及实际 Preview rect/size；禁止直接按屏幕坐标裁原图。该规则来自 Mr.Mushroom、Mr.Rock 的真机裁切偏移修复。
- 拍照、相册和 Demo 输入必须保留准确的 `RecognitionContext`，避免丢失来源、地理位置或 Coach Mark 状态。
- 识别取消、重试、购买后继续和页面 dismiss 必须取消或收敛正在运行的 Task，不能产生重复识别或重复保存。
- App 原有 Coach Mark 和引导步骤可覆盖在共享页面上，不要为单个 App 把引导逻辑塞进 Package。

## 9. Paywall 接入

- 在 App 内实现 `BillingClient` Adapter，把现有 RevenueCat 服务映射为 `BillingProduct`、`PurchaseResult` 和 `EntitlementState`。
- Adapter 和持有 UI 状态的 Billing 服务标注 `@MainActor`；不要从共享 Package 直接访问 RevenueCat 单例。
- 产品 ID、产品分组、Lifetime 历史 ID、免费次数、付费次数和持久化 key 必须沿用 App 当前生产配置。
- 购买结果必须区分成功、用户取消和失败；取消不能展示为购买失败，也不能记账。
- Restore 与前后台刷新必须重新同步权益；Lifetime 生效后立即更新设置页和识别入口。
- Consumable 购买只在确认交易成功后增加额度；同一 transaction ID 必须幂等，不能重复入账。
- 迁移 `UserDefaults` key 或额度消费顺序属于数据迁移，必须单独设计测试，不能在 UI 接入时顺手更改。
- Paywall 外观选择必须明确：iNature/NatureEar 使用 iNature 风格，Mr.Mushroom/Mr.Rock 使用 Card Selection 风格；业务状态由同一个 `PaywallStore` 驱动。
- Paywall 展示、产品选择、购买结果、Restore 结果等事件通过闭包映射到 App Analytics。

## 10. 推荐迁移顺序

每个 App 按以下顺序独立提交，避免把工程搬迁、业务重构和共享组件接入混在一个不可审查的提交中：

1. 记录迁移前行为：产品 ID、权益、额度、历史数据、关键埋点和主要页面截图。
2. 接入兄弟仓库路径、所需 Package 产品、推荐资源 Build Phase 和 CI bootstrap。
3. 建立 App Theme、Billing Adapter、Recognition Adapter 等薄适配层。
4. 接入 Settings；先保留 App 专属 section，再逐步删除重复通用组件。
5. 有拍照业务时接入 Camera、Crop 和 Processing，并对比迁移前后的实际裁剪结果。
6. 接入 Paywall，验证产品、价格、购买、取消、Restore、Lifetime 和 Consumable。
7. 全部行为通过后再删除旧页面和旧状态机。

## 11. 验收清单

### 工程与 CI

- 本地全新 clone 后能够解析兄弟路径 Package。
- Xcode Cloud 的干净环境能够先拉取 `AppRecommendations` 再解析工程。
- XcodeGen 重新生成后没有丢失 Package、Build Phase 或输入输出文件。
- AppRecommendations 缺失时 bootstrap 给出明确错误。

### 推荐 App

- 当前 App 不显示，`isVisible: false` 的 App 不显示。
- 所有可见卡片的文案、渐变、图标和 App Store URL 正确。
- 无网络、URL 无法打开、JSON 缺失、图标缺失时有安全降级。

### Settings

- Light、Dark、动态字体、小屏 iPhone 和 iPad 布局正常。
- Appearance、系统设置、法律链接、反馈、评分和 Restore 行为正常。
- History push、侧滑返回、清空确认、记录和缓存图片清理正常。
- Favorites 等 App 专属入口没有因共享历史组件调整而丢失。

### Capture

- 相机权限未决定、允许、拒绝、受限状态正确。
- 相机与相册图片方向正确；不同屏幕比例下裁剪结果与取景框一致。
- 缩放、拖动、重置、取消、重试和识别中 dismiss 正常。
- 免费额度、付费额度和 Lifetime 对识别入口的拦截与消费正确。

### Paywall

- 产品全量、部分缺失和加载失败状态可用。
- 购买成功、用户取消、失败、Restore 成功和无可恢复权益均正确。
- 老用户 Lifetime 和剩余额度不丢失；重复交易不重复记账。
- 前后台切换后权益与价格能够刷新。

## 12. 构建与测试

`BioScanKit` 是 iOS Package，包含 UIKit。不要用宿主 macOS 的 `swift build` 作为最终结论；使用 iOS Simulator SDK：

```sh
cd AppRecommendations/BioScanKit
xcodebuild \
  -scheme BioScanKit \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath /tmp/BioScanKit-DerivedData \
  CODE_SIGNING_ALLOWED=NO \
  build
```

同时必须构建实际 App scheme，因为只有 App 构建才能覆盖本地 Package 路径、资源复制、Adapter、Bundle 和部署版本组合。高风险业务还需运行以下测试：

- `BioScanCaptureTests`：裁剪几何、图片方向和预览映射。
- `BioScanPaywallTests`：额度、交易幂等、Lifetime 和 Restore。
- `BioScanSettingsTests`：推荐 App 解码、过滤和安全降级。
- App 自有测试：历史清理、识别结果保存、RevenueCat Adapter 和路由。

## 13. 公共 API 变更规则

- 公共 API 变更前先搜索 iNature、Mr.Mushroom、Mr.Rock、NatureEar 的全部调用点。
- 破坏性变更必须在同一批工作中更新所有消费者并分别构建验证。
- 新增可配置项应有保持现有行为的默认值；只有明确淘汰旧行为时才移除参数。
- 公共组件调整不得静默删除 App 专属入口。例如共享 History API 移除 Favorites 后，各 App 的 Favorites 必须继续由 App 自己维护。
- 修复共享相机几何或购买账本时必须先补回归测试，再迁移 App 侧临时 workaround。
- 每个 App 的接入与验收独立提交；`AppRecommendations` 的共享变更先提交并推送，再更新消费者，避免 CI 拉取到不存在的 API。
