# advertise 库使用文档

`advertise` 是项目内的 Android 广告与增长能力库，宿主 App 通过 `AdvertiseSdk` 完成初始化、广告展示、广告预加载、UMP 隐私同意、埋点、推送通知、Remote Config、归因与安全校验等能力接入。

当前库的包名空间为 `com.pdffox.adv`，模块名为 `:advertise`，最低支持 `minSdk 29`。

## 目录

- 功能范围
- 快速接入路径
- Gradle 接入
- Manifest 配置
- 初始化与生命周期约定
- 配置创建与配置来源
- 广告位 key 与广告策略
- 广告展示 API
- 广告预加载
- UMP 隐私同意
- 推送与通知
- 埋点与用户属性
- Remote Config 约定
- 状态读取与本地存储
- 调试排查与验证清单
- 常见问题

## 文档约定

| 名称 | 含义 |
| --- | --- |
| 宿主 App | 引入 `:advertise` 模块的业务 App，例如当前 `:app` 模块。 |
| SDK | 本文中的 `advertise` Android library。 |
| `isTest` | SDK 测试环境开关，通常传 `BuildConfig.DEBUG`。 |
| `areaKey` | 广告位业务 key，必须和本地/远程广告策略中的 `areakey` 完全一致。 |
| 本地策略 | 随包发布的 raw 资源，例如 `advertise/src/main/res/raw/ad_policy.json`。 |
| 远程策略 | Firebase Remote Config 下发的 `ad_policy`、`adload_config`、`native_ad_policy` 等参数。 |
| placeholder | Gradle `manifestPlaceholders`，用于写入 Manifest meta-data 等宿主参数；SDK 组件 enabled 状态已固定开启，不再通过 placeholder 控制。 |

## 功能范围

- AdMob 广告：开屏、插屏、Banner、Native。
- 广告策略：本地 `ad_policy.json` 与 Firebase Remote Config 下发策略。
- 广告预加载：按触发时机控制开屏、插屏、Native 预加载。
- UMP：GDPR/隐私同意弹窗、隐私选项入口。
- 统计与归因：Firebase Analytics、ThinkingData、Singular、Facebook、TikTok。
- 推送与通知：FCM、常驻通知、通知路由、删除监听触发通知。
- 安全校验：包名、签名、debuggable、调试器附加检测。
- Play Integrity：可选完整性校验。

## 快速接入路径

只接广告展示时，最小接入链路如下：

1. 在 `settings.gradle.kts` 引入 `:advertise`，宿主 App 添加 `implementation(project(":advertise"))`。
2. 在宿主 `AndroidManifest.xml` 写入 AdMob App ID meta-data，并保证它和 `AdvertiseSdkConfig.adMob.appId` 一致。
3. 在宿主侧创建 `AdvertiseSdkConfig`，至少配置 `legal`、`adMob`、`resources(adPolicyRawResId = ...)`。
4. 在 `Application.onCreate()` 调用 `AdvertiseSdk.init(application, BuildConfig.DEBUG, config)`。
5. 在页面里使用 `showOpenAd`、`showInterstitialAd`、`getBannerAd` 或 `rememberNativeAd`。
6. 根据需要逐步配置 Remote Config、归因、推送、通知、安全校验和 Play Integrity 所需参数。

推荐按能力分阶段接入：

| 阶段 | 需要配置 | 验证点 |
| --- | --- | --- |
| 广告基础能力 | `adMob`、`ad_policy.json`、AdMob manifest meta-data | 开屏、插屏、Banner 能展示或正确执行关闭回调 |
| Native 广告 | Remote Config 的 `native_ad_policy`、`native_ad_ids` | `rememberNativeAd` 能返回 `NativeAd` |
| 预加载策略 | Remote Config 的 `adload_config` | `canPreload*()` 和 `preload*()` 行为符合后台配置 |
| 推送通知 | `firebase`、`push`、`notifications`、`push.json` | FCM topic 正确，通知点击能带 route 回到宿主 |
| 发布安全 | `safe`、Release 签名、Play Integrity | Release 包不会误杀，异常包名/签名会被拦截 |

### 最小可运行配置

如果只验证 AdMob 开屏、插屏和 Banner，可以先关闭 Firebase、Remote Config、归因、推送和安全校验，只保留本地广告策略：

```kotlin
object MinimalAdvertiseConfigFactory {
    fun create(context: Context): AdvertiseSdkConfig {
        return AdvertiseSdkConfigs.create(context, BuildConfig.DEBUG) {
            legal(
                privacyUrl = "https://example.com/privacy",
                termsUrl = "https://example.com/terms",
            )
            resources(
                adPolicyRawResId = com.pdffox.adv.R.raw.ad_policy,
            )
            adMob(
                enabled = true,
                appId = "ca-app-pub-xxx~yyy",
                bannerId = "ca-app-pub-xxx/banner",
                interstitialId = "ca-app-pub-xxx/interstitial",
                nativeId = "ca-app-pub-xxx/native",
                openId = "ca-app-pub-xxx/open",
            )
        }
    }
}
```

对应 Manifest 至少需要：

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="${advAdmobAppId}" />
```

对应 Gradle placeholder 至少需要：

```kotlin
manifestPlaceholders["advAdmobAppId"] = "ca-app-pub-xxx~yyy"
```

这个配置可以验证 `showOpenAd`、`showInterstitialAd`、`getBannerAd`。Native 仍需要额外下发 `native_ad_policy` 和 `native_ad_ids`。

## 1. Gradle 接入

在 `settings.gradle.kts` 中包含模块：

```kotlin
include(":advertise")
```

在宿主 App 模块依赖中引入：

```kotlin
dependencies {
    implementation(project(":advertise"))
}
```

如果启用 Firebase 能力，宿主 App 通常还需要应用 Google Services 插件，并提供 `google-services.json`：

```kotlin
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}
```

根工程仓库需要包含 Google、Maven Central 以及广告聚合依赖所需的三方仓库。当前工程已经在 `settings.gradle.kts` 中配置。

## 2. Manifest 配置

`advertise` 模块会合并网络、通知、前台服务、开机广播、FileProvider、插屏 Activity 等声明。宿主 App 需要补充广告平台的 meta-data；SDK 组件启用状态在库 Manifest 中固定开启。

### 2.1 AdMob 与 Facebook meta-data

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="${advAdmobAppId}" />

    <meta-data
        android:name="com.facebook.sdk.ApplicationId"
        android:value="${advFacebookAppId}" />

    <meta-data
        android:name="com.facebook.sdk.ClientToken"
        android:value="${advFacebookClientToken}" />
</application>
```

注意：`advAdmobAppId` 必须与初始化时 `AdvertiseSdkConfig.adMob.appId` 一致。Debug 下不一致会直接抛错，Release 下会输出 warning。

### 2.2 组件启用状态

SDK Manifest 中以下组件已显式固定为 `android:enabled="true"`：

| 组件 | 当前行为 |
| --- | --- |
| `com.pdffox.adv.notification.CommonService` | 固定开启 |
| `com.pdffox.adv.push.MyFirebaseMessagingService` | 固定开启 |
| `com.pdffox.adv.push.ServiceStarterJobService` | 固定开启 |
| `com.pdffox.adv.notification.NotificationDeletedReceiver` | 固定开启 |
| `com.pdffox.adv.notification.BootReceiver` | 固定开启 |
| SDK FileProvider | 固定开启 |

宿主不再通过 enabled placeholder 关闭这些组件。若特殊宿主必须移除某个组件，应通过宿主 Manifest merge 规则显式覆盖。

Android 13 及以上展示通知前，宿主 App 仍需要自行申请 `POST_NOTIFICATIONS` 运行时权限。

### 2.3 权限与组件矩阵

`advertise` 模块已经声明基础权限，运行时授权和平台后台配置仍由宿主负责。

| 能力 | Manifest/权限 | 运行时配置 | 宿主需要做什么 |
| --- | --- | --- | --- |
| AdMob 广告 | `INTERNET`、`ACCESS_NETWORK_STATE`、`com.google.android.gms.ads.APPLICATION_ID` | `adMob.enabled = true` | 配置真实 App ID 和广告位 ID，Debug 使用测试 ID 或测试设备 |
| UMP | `INTERNET` | 无独立开关 | 在启动页调用 `initConsent`/`showSplashConsent`，设置页按需暴露 `showPrivacyOptions` |
| Firebase Analytics | `google-services.json` | `firebase.analyticsEnabled = true` | 应用 Google Services 插件，确认 Firebase 项目包名一致 |
| FCM 推送 | `MyFirebaseMessagingService` 固定 enabled | `firebase.messagingEnabled = true`、`push.firebaseMessagingServiceEnabled = true` | 配置 `google-services.json`，确认 topic 和后台发送目标一致 |
| 常驻通知 | `FOREGROUND_SERVICE`、`FOREGROUND_SERVICE_SPECIAL_USE`、`CommonService` 固定 enabled | `push.persistentServiceEnabled = true`、`notifications.enabled = true` | Android 13+ 申请通知权限，在合适时机调用 `ensurePersistentNotificationServiceRunning` |
| 开机恢复 | `RECEIVE_BOOT_COMPLETED`、`BootReceiver` 固定 enabled | `push.bootReceiverEnabled = true` | 确认设备/系统允许开机广播，必要时做厂商兼容兜底 |
| 通知删除监听 | `NotificationDeletedReceiver` 固定 enabled | `push.notificationDeletedReceiverEnabled = true` | 如果宿主自定义 action，需要同步配置 `notificationDeletedAction` |
| FileProvider 附件 | `FileProvider` 固定 enabled | `push.fileProviderEnabled = true` | 默认 authority 为 `${applicationId}.pdffox.adv.fileprovider`，自定义时同步配置 `fileProviderAuthority` |
| 删除监听推送 | 媒体/文件读取权限由宿主业务申请 | `push.deletionObserverEnabled = true` | 确认宿主已获得对应文件/媒体访问权限 |
| 安全校验 | 无额外权限 | `safe.enabled = true` | Release 配置包名和签名 SHA-256，Debug 默认不强制 |
| Play Integrity | Play Integrity 依赖和 Google Cloud 配置 | `playIntegrity.enabled = true`、`cloudProjectNumber > 0` | 配置 Google Cloud project number 和服务端 token 解析 |

## 3. 初始化

推荐在宿主 `Application.onCreate()` 中初始化。`AdvertiseSdk.init` 是 `suspend` 方法，宿主可使用 `runBlocking` 保证广告/配置在首屏前完成，也可以在应用自己的启动协程中调用。

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        kotlinx.coroutines.runBlocking {
            AdvertiseSdk.init(
                context = this@MyApp,
                isTest = BuildConfig.DEBUG,
                sdkConfig = AdvertiseConfigFactory.create(this@MyApp),
            )
        }
    }
}
```

如果宿主可以直接继承库内 Application，也可以继承 `AdvApplicaiton`，但多数情况下更推荐保留宿主自己的 `Application`，直接调用 `AdvertiseSdk.init`。

### 3.1 初始化时序

推荐启动时序：

```text
Application.onCreate
  -> 初始化宿主依赖注入/日志等基础设施
  -> 创建 AdvertiseSdkConfig
  -> AdvertiseSdk.init(application, isTest, config)
  -> MainActivity.onCreate
  -> 启动页 UMP / 预加载 / 开屏广告
  -> 进入业务页面展示插屏、Banner、Native
```

关键约定：

- `AdvertiseSdk.init` 应只在进程启动后初始化一次；重复调用没有作为常规接入方式设计。
- 初始化会注册 Activity 生命周期监听，用于当前 Activity、前后台状态、开屏广告、强更和部分用户属性采集。
- 展示类 API 需要传入有效 `Activity` 或 `Context`；涉及 UI 展示的调用建议在主线程发起。
- 插屏和开屏的关闭回调需要承担“继续业务流程”的职责；无广告、广告被策略过滤、广告网络未开启时也会走回调或等价流程。
- 如果宿主有冷启动耗时要求，可以把 Remote Config、归因等非首帧必需能力放在启动策略里评估，但 `AdvertiseSdk.init` 仍应早于广告展示调用。

### 3.2 页面生命周期建议

| 场景 | 建议 |
| --- | --- |
| 启动页 | 先处理 UMP，再按 `canPreload*()` 预加载，最后展示首开开屏广告。 |
| 从权限页/系统设置返回 | 设置 `AdvertiseSdk.suppressNextAppOpenAd = true`，避免返回时打断用户授权流程。 |
| 页面跳转前插屏 | 把导航动作放到 `showInterstitialAd` 的 `onClosed` 中。 |
| Compose Banner | 用 `remember` 持有 `getBannerAd()` 返回值，用 `BannerAd` 负责销毁。 |
| Compose Native | 用 `rememberNativeAd()` 获取状态，由页面自己的 `DisplayNativeAd` 负责素材布局。 |
| 常驻通知 | 在 Activity `onResume` 或业务合适时机调用 `ensurePersistentNotificationServiceRunning()` 兜底。 |

## 4. 创建配置

建议宿主侧集中提供一个配置工厂，避免页面层直接依赖 BuildConfig 字段。

```kotlin
object AdvertiseConfigFactory {
    fun create(context: Context): AdvertiseSdkConfig {
        return AdvertiseSdkConfigs.create(context, BuildConfig.DEBUG) {
            legal(
                privacyUrl = BuildConfig.ADV_PRIVACY_URL,
                termsUrl = BuildConfig.ADV_TERMS_URL,
            )

            defaultTopic(BuildConfig.ADV_DEFAULT_TOPIC)
            resources(
                adPolicyRawResId = com.pdffox.adv.R.raw.ad_policy,
                cloudCidrsRawResId = com.pdffox.adv.R.raw.cloud,
                googleCidrsRawResId = com.pdffox.adv.R.raw.google,
                pushConfigRawResId = R.raw.push,
            )

            server(
                enabled = true,
                releaseHost = BuildConfig.ADV_SERVER_RELEASE_HOST,
                testHost = BuildConfig.ADV_SERVER_TEST_HOST,
                parseTokenKey = BuildConfig.ADV_SERVER_PARSE_TOKEN_KEY,
            )

            firebase(
                analyticsEnabled = true,
                messagingEnabled = true,
                subscribeDefaultTopic = true,
            )
            remoteConfig(enabled = true)

            thinking(
                enabled = true,
                appKey = BuildConfig.ADV_THINKING_APP_KEY,
                serverUrl = BuildConfig.ADV_THINKING_SERVER_URL,
            )
            singular(
                enabled = true,
                apiKey = BuildConfig.ADV_SINGULAR_API_KEY,
                secret = BuildConfig.ADV_SINGULAR_SECRET,
            )

            adMob(
                enabled = true,
                appId = BuildConfig.ADV_ADMOB_APP_ID,
                bannerId = BuildConfig.ADV_ADMOB_BANNER_ID,
                interstitialId = BuildConfig.ADV_ADMOB_INTERSTITIAL_ID,
                nativeId = BuildConfig.ADV_ADMOB_NATIVE_ID,
                openId = BuildConfig.ADV_ADMOB_OPEN_ID,
            )

            facebook(
                enabled = true,
                appId = BuildConfig.ADV_FACEBOOK_APP_ID,
                clientToken = BuildConfig.ADV_FACEBOOK_CLIENT_TOKEN,
            )
            tiktok(
                enabled = true,
                accessToken = BuildConfig.ADV_TIKTOK_ACCESS_TOKEN,
                ttAppId = BuildConfig.ADV_TIKTOK_TT_APP_ID,
                appId = BuildConfig.ADV_TIKTOK_APP_ID,
            )

            safe(
                enabled = true,
                expectedSignatures = BuildConfig.ADV_SAFE_EXPECTED_SIGNATURES,
                rejectDebuggableBuilds = true,
                rejectDebuggerAttached = true,
                killProcessOnFailure = true,
            )

            push(
                enabled = true,
                persistentServiceEnabled = true,
                firebaseMessagingServiceEnabled = true,
                serviceStarterJobEnabled = true,
                bootReceiverEnabled = true,
                notificationDeletedReceiverEnabled = true,
				fileProviderEnabled = true,
				deletionObserverEnabled = true,
				sceneKeys = PushSceneKeyConfig(
					imageDeleted = "delete_photos",
					videoDeleted = "delete_videos",
					fileDeleted = "delete_files",
                ),
            )

            notifications(
                NotificationFeatureConfig(
                    enabled = true,
                    smallIconResId = R.mipmap.ic_launcher,
                    persistentContentText = context.getString(R.string.app_name),
                    persistentActions = listOf(
                        NotificationActionConfig(route = "Home", label = "Home"),
                    ),
                    routeMappings = listOf(
                        NotificationRouteMapping(rawRoute = "/home", route = "Home"),
                    ),
                )
            )

            playIntegrity(
                enabled = true,
                cloudProjectNumber = BuildConfig.ADV_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER,
            )
        }
    }
}
```

配置说明：

| 配置段 | 作用 |
| --- | --- |
| `legal` | 隐私政策与用户协议 URL，供宿主设置页、合规弹窗和 SDK 内部读取。 |
| `defaultTopic` | FCM 默认订阅 topic。Debug 为空时回退 `debug-all`，Release 为空时回退 `all`。 |
| `resources` | 本地广告策略、IP 段、推送配置资源。`pushConfigRawResId` 默认为 `0`，启用推送时宿主应传自己的 raw 资源。 |
| `server` | 后端检查、配置、IP、推送、Play Integrity token 解析接口地址。 |
| `firebase` | Firebase Analytics、Messaging 与默认 topic 订阅开关。 |
| `remoteConfig` | Firebase Remote Config 拉取广告策略、预加载策略、Native 策略、通知配置等。 |
| `thinking` | ThinkingData 初始化与事件/用户属性上报。 |
| `singular` | Singular 归因和广告收入上报。 |
| `adMob` | AdMob App ID 与各广告位 ID。 |
| `facebook` | Facebook SDK 初始化参数。 |
| `tiktok` | TikTok Business SDK 初始化参数。 |
| `safe` | 包名、签名、debuggable、调试器安全校验。Debug 默认不强制签名与反调试，可通过 `enforceInDebugBuilds = true` 改变。 |
| `push` | 推送、常驻通知、开机恢复、删除监听、FileProvider 等运行时能力开关。 |
| `notifications` | 常驻通知展示内容、操作按钮和后端 route 到宿主 route 的映射。 |
| `playIntegrity` | Play Integrity 请求参数。Debug 默认不执行，除非 `runInDebugBuilds = true`。 |

### 4.1 配置来源建议

宿主 App 推荐把敏感参数和环境差异参数放在 Gradle property、`local.properties` 或 CI 环境变量中，再写入 `BuildConfig` 和仍需保留的 manifest placeholders。当前 App 的读取优先级是：

```text
Gradle property -> local.properties -> 环境变量 -> 默认值
```

推荐字段分组如下：

| 分组 | BuildConfig/placeholder 示例 | 说明 |
| --- | --- | --- |
| 合规链接 | `ADV_PRIVACY_URL`、`ADV_TERMS_URL` | 隐私政策和用户协议 URL |
| 后端 | `ADV_SERVER_RELEASE_HOST`、`ADV_SERVER_TEST_HOST`、`ADV_SERVER_PARSE_TOKEN_KEY` | 后端检查、配置、IP、推送、token 解析接口 |
| Firebase | `ADV_DEFAULT_TOPIC` | FCM 默认 topic；Firebase 能力固定开启 |
| ThinkingData | `ADV_THINKING_APP_KEY`、`ADV_THINKING_SERVER_URL` | ThinkingData 事件和用户属性上报 |
| Singular | `ADV_SINGULAR_API_KEY`、`ADV_SINGULAR_SECRET` | 归因和广告收入上报 |
| AdMob | `advAdmobAppId`、`ADV_ADMOB_APP_ID`、`ADV_ADMOB_BANNER_ID`、`ADV_ADMOB_INTERSTITIAL_ID`、`ADV_ADMOB_NATIVE_ID`、`ADV_ADMOB_OPEN_ID` | `advAdmobAppId` 写入 Manifest，其他字段写入 SDK 配置 |
| Facebook | `advFacebookAppId`、`advFacebookClientToken`、`ADV_FACEBOOK_APP_ID`、`ADV_FACEBOOK_CLIENT_TOKEN` | Manifest 和 SDK 配置要保持一致 |
| TikTok | `ADV_TIKTOK_ACCESS_TOKEN`、`ADV_TIKTOK_APP_ID`、`ADV_TIKTOK_TT_APP_ID` | TikTok Business SDK 参数 |
| 安全 | `ADV_SAFE_EXPECTED_SIGNATURES` | Release 发布前必须确认签名列表；安全能力固定开启 |
| 推送 | `ADV_PUSH_SCENE_IMAGE_DELETED`、`ADV_PUSH_SCENE_VIDEO_DELETED`、`ADV_PUSH_SCENE_FILE_DELETED` | Push 场景 key；SDK 组件 enabled 状态固定开启 |
| 通知/完整性 | `ADV_PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER` | 通知能力固定开启；Play Integrity 项目号仍需配置 |

注意：运行时能力和 Manifest 组件 enabled 状态已经固定开启；仍需配置的是密钥、ID、topic、场景 key、签名白名单、Play Integrity 项目号等非开关参数。

## 5. 广告位 key 与广告策略

宿主用字符串定义广告位，例如：

```kotlin
object AreaKey {
    const val openPageAdv = "openPageAdv"
    const val enterFeatureAdv = "enterFeatureAdv"
    const val recoverPageBottomAdv = "recoverPageBottomAdv"
}

object AreaKeyNative {
    const val homeNativeAdv = "homeNativeAdv"
}
```

本地广告策略默认读取 `AdvertiseResourcesConfig.adPolicyRawResId`，格式如下：

```json
{
  "package_name": "",
  "platform": "android",
  "global_ad_switch": true,
  "first_open_enabled": true,
  "limited": 50,
  "limited_loadtime_seconds": 86400,
  "ad_network": {
    "aggregator": "admob"
  },
  "ad_units": [
    {
      "areakey": "openPageAdv",
      "ad_format": "open",
      "rate": 1.0,
      "frequency_caps": {
        "max_per_hour": 4,
        "max_per_day": 20,
        "interval_seconds": 15
      }
    }
  ]
}
```

字段说明：

- `package_name`：为空表示所有包名可用；非空时必须等于当前宿主包名，否则策略会被忽略。
- `global_ad_switch`：总广告开关。
- `first_open_enabled`：首开是否允许展示开屏广告。
- `limited`/`limited_loadtime_seconds`：全局播放次数窗口。
- `ad_units[].areakey`：页面传入的广告位 key。
- `ad_units[].ad_format`：`open`、`interstitial`、`banner`。
- `ad_units[].rate`：广告位命中概率，`0.0` 到 `1.0`。
- `frequency_caps`：单广告位每小时、每日、最小间隔控制。

Native 广告不走本地 `ad_policy.json` 的 `ad_format`，而是由 Remote Config 中的 `native_ad_policy` 控制。未下发 Native 策略时，普通 Native 广告位会被过滤；调试页可使用特殊 key `debug_page` 绕过策略。

## 6. 广告展示 API

### 6.1 开屏广告

```kotlin
AdvertiseSdk.showOpenAd(
    activity = activity,
    areaKey = AreaKey.openPageAdv,
    onCloseListener = AdvertiseSdk.OpenAdCloseListener {
        // 广告关闭或无可用广告后继续启动流程
    },
    onLoadedListener = AdvertiseSdk.OpenAdLoadedListener {
        // 广告已加载
    },
    onPaidListener = AdvertiseSdk.OpenAdPaidListener { valueMicros ->
        // AdMob paid event，单位为 micros
    },
)
```

可通过以下状态控制开屏广告：

```kotlin
AdvertiseSdk.isAppOpenAdEnabled = true
AdvertiseSdk.suppressNextAppOpenAd = true
```

- `isAppOpenAdEnabled`：控制前后台切换时 AppOpenHelper 是否工作。
- `suppressNextAppOpenAd`：跳过下一次开屏展示，适合从系统设置、权限页、文件选择器返回时使用。

### 6.2 插屏广告

```kotlin
AdvertiseSdk.showInterstitialAd(
    activity = activity,
    areaKey = AreaKey.enterFeatureAdv,
) {
    // 广告关闭、无广告或未启用广告网络后继续原业务动作
}
```

`onClosed` 一定要放业务后续动作，例如导航、返回首页、进入结果页等，避免广告页未关闭时页面状态已经跳转。

### 6.3 Banner 广告

View 系统：

```kotlin
val adView = AdvertiseSdk.getBannerAd(context, AreaKey.recoverPageBottomAdv)
adView?.let { container.addView(it) }
```

Compose：

```kotlin
val context = LocalContext.current
val banner = remember {
    AdvertiseSdk.getBannerAd(context, AreaKey.recoverPageBottomAdv)
}

banner?.let {
    BannerAd(adView = it, modifier = Modifier.fillMaxWidth())
}
```

`BannerAd` 会在 Composable dispose 时销毁 `AdView`。

### 6.4 Native 广告

库提供 `rememberNativeAd` 负责从 Native 广告池取广告，并按 SDK 内部 Native 刷新间隔自动刷新。默认间隔为 30 秒，可由 Remote Config 的 `native_refresh_time` 覆盖。

```kotlin
val nativeAdState = rememberNativeAd(
    areaKey = AreaKeyNative.homeNativeAdv,
    refreshImmediately = true,
    shouldRefreshImmediately = { true },
    shouldAutoRefresh = { true },
)

nativeAdState.value?.let { nativeAd ->
    DisplayNativeAd(nativeAd)
}
```

宿主需要用 Google Mobile Ads 的 `NativeAdView` 规范包裹并注册广告素材 view。当前 App 已有示例组件：`app/src/main/java/com/datatool/photorecovery/view/widget/NativeAdView.kt` 与 `DisplayNativeAd.kt`。

关键要求：

- `NativeAdView` 必须是最外层原生广告容器。
- `headlineView`、`bodyView`、`callToActionView`、`iconView`、`mediaView` 等素材 view 必须正确注册。
- CTA 按钮不要自己处理点击事件，否则会截断 NativeAd 的点击归因；当前示例里的 `NativeAdButton` 只是展示容器。
- Native 广告位需要 Remote Config 下发 `native_ad_policy`，否则策略检查不通过。

### 6.5 `AdvertiseSdk` 对外 API 速查

| API | 返回值 | 用途 |
| --- | --- | --- |
| `init(context, isTest, sdkConfig)` | `Unit` | 初始化 SDK、配置、广告、Firebase、Remote Config、归因、通知、安全校验等能力 |
| `showOpenAd(activity, areaKey, onClose, onLoaded, onPaid)` | `Unit` | 展示开屏广告 |
| `showInterstitialAd(activity, areaKey, onClosed)` | `Unit` | 展示插屏广告 |
| `getBannerAd(context, areaKey)` | `ViewGroup?` | 获取 Banner View，可能因策略、用户状态或广告网络关闭返回 null |
| `preloadOpen(context)` | `Unit` | 预加载开屏广告 |
| `preloadInterstitial(context)` | `Unit` | 预加载插屏广告 |
| `preloadNative(context, onAdGroupLoaded)` | `Unit` | 填充 Native 广告池 |
| `canPreloadOpen(loadTimeKey)` | `Boolean` | 查询某个触发时机是否允许预加载开屏 |
| `canPreloadInterstitial(loadTimeKey)` | `Boolean` | 查询某个触发时机是否允许预加载插屏 |
| `canPreloadNative(loadTimeKey)` | `Boolean` | 查询某个触发时机是否允许预加载 Native |
| `initConsent(activity, onComplete)` | `Boolean` | 初始化 UMP 同意状态，返回是否有缓存流程状态 |
| `showSplashConsent(activity, onComplete)` | `Unit` | 启动页展示 UMP 同意弹窗 |
| `showPrivacyOptions(activity)` | `Unit` | 展示 UMP 隐私选项弹窗 |
| `logEvent(eventName, params)` | `Unit` | 上报普通事件 |
| `setSuperProperties(properties)` | `Unit` | 设置 ThinkingData 公共属性 |
| `setUserOnceAttr(key, value)` | `Unit` | 设置一次性用户属性 |
| `setUserAttr(key, value)` | `Unit` | 设置用户属性 |
| `getPreferenceString(key, defaultValue)` | `String` | 读取 SDK Preference 字符串 |
| `putPreferenceString(key, value)` | `Unit` | 写入 SDK Preference 字符串 |
| `sendDebugNotification(context, notificationType, configName)` | `Unit` | 发送调试通知 |
| `ensurePersistentNotificationServiceRunning(context)` | `Unit` | 确保常驻通知服务运行 |

| 属性 | 类型 | 用途 |
| --- | --- | --- |
| `guidePageSwapTime` | `Long` | 引导页自动切换间隔 |
| `isAppOpenAdEnabled` | `Boolean` | 前后台切换开屏广告总开关，可读写 |
| `suppressNextAppOpenAd` | `Boolean` | 跳过下一次开屏广告，可读写 |
| `shouldIgnoreGuide` | `Boolean` | 兼容旧宿主调用，固定返回 `false` |
| `hasOpenLaunchPage` | `Boolean` | 兼容旧宿主调用，固定返回 `false` |
| `isFirstOpenAdEnabled` | `Boolean` | 首开广告是否启用 |
| `isGoogleIp` | `Boolean` | 当前 IP 是否命中 Google IP 判断 |
| `isPaidUser` | `Boolean` | 当前用户是否被判定为付费/屏蔽广告用户 |
| `shouldSuppressAdsForCurrentUser` | `Boolean` | 宿主是否应为当前用户隐藏广告 |
| `isNature` | `Boolean` | Singular 归因是否自然量 |
| `topic` | `String` | 当前 FCM topic |
| `privacyUrl`/`termsUrl` | `String` | 合规链接 |
| `isPrivacyOptionsRequired` | `Boolean` | 是否需要展示隐私选项入口 |

### 6.6 失败与降级行为

广告接口默认按“广告失败不阻塞业务”设计：

| 场景 | 行为 |
| --- | --- |
| 未启用 AdMob | 插屏会直接执行 `onClosed`，Banner/Native 返回 null。 |
| 当前用户应屏蔽广告 | 非 Debug 下 Banner/Native 返回 null，宿主可用 `shouldSuppressAdsForCurrentUser` 隐藏广告容器。 |
| `areaKey` 不在策略中 | 对应广告位不展示。 |
| 频控或概率未命中 | 对应广告位不展示。 |
| 插屏加载失败或无缓存 | 关闭回调继续业务流程。 |
| Banner 返回 null | 宿主不要占位或应展示自己的空态/折叠容器。 |
| Native 返回 null | 可以先不渲染广告位，等待 `onAdGroupLoaded` 或下一次刷新。 |
| UMP 初始化失败 | 回调仍会结束，宿主应继续启动流程并按合规策略决定是否展示广告。 |

页面层不要把广告展示成功作为业务流程的前置条件。广告只影响收益和展示，不应影响用户进入核心功能。

## 7. 广告预加载

SDK 内置以下预加载触发时机常量：

```kotlin
AdvertiseSdk.LOAD_TIME_OPEN_APP
AdvertiseSdk.LOAD_TIME_PLAY_FINISH
AdvertiseSdk.LOAD_TIME_ENTER_BACKGROUND
AdvertiseSdk.LOAD_TIME_RECEIVE_NOTIFICATION
AdvertiseSdk.LOAD_TIME_ENTER_FEATURE
```

使用方式：

```kotlin
if (AdvertiseSdk.canPreloadOpen(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
    AdvertiseSdk.preloadOpen(context)
}

if (AdvertiseSdk.canPreloadInterstitial(AdvertiseSdk.LOAD_TIME_ENTER_FEATURE)) {
    AdvertiseSdk.preloadInterstitial(context)
}

if (AdvertiseSdk.canPreloadNative(AdvertiseSdk.LOAD_TIME_OPEN_APP)) {
    AdvertiseSdk.preloadNative(context)
}
```

是否允许预加载由本地默认配置或 Remote Config 的 `adload_config` 控制。默认值在 `AdConfig` 中，Remote Config 下发后会覆盖。

## 8. UMP 隐私同意

初始化同意状态：

```kotlin
val hasCachedConsent = AdvertiseSdk.initConsent(activity) { success ->
    // success 表示 UMP 流程完成，不代表一定可以展示广告
}
```

启动页展示同意弹窗：

```kotlin
AdvertiseSdk.showSplashConsent(activity) {
    // 同意流程结束后继续启动流程
}
```

设置页展示隐私选项：

```kotlin
if (AdvertiseSdk.isPrivacyOptionsRequired) {
    AdvertiseSdk.showPrivacyOptions(activity)
}
```

## 9. 推送与通知

启用推送通知能力需要同时满足：

1. `AdvertiseSdkConfig.push.enabled = true`。
2. 对应功能的运行时开关为 `true`，例如 `firebaseMessagingServiceEnabled`、`persistentServiceEnabled`。
3. SDK Manifest 组件保持固定 enabled。
4. 宿主传入 `resources(pushConfigRawResId = R.raw.push)`。
5. Android 13 及以上已获得通知权限。

推送场景 key 在 `PushSceneKeyConfig` 中映射：

```kotlin
PushSceneKeyConfig(
    imageDeleted = "delete_photos",
    videoDeleted = "delete_videos",
    fileDeleted = "delete_files",
)
```

`push.json` 的核心格式：

```json
{
  "first_trigger_time": 21600,
  "scene": {
    "delete_photos": {
      "trigger_interval": 300,
      "enabled": true,
      "messages": [
        {
          "title": "Title",
          "content": "Content",
          "route": "/home",
          "keys": [
            {
              "language": "es",
              "title": "Titulo",
              "content": "Contenido"
            }
          ]
        }
      ]
    }
  }
}
```

通知触发名需要和 SDK 内部发送时使用的 name 对齐：

| 系统/业务场景 | `notification_config.triggers[].name` | 说明 |
| --- | --- | --- |
| App 进入后台 | `press_key_home` | Activity 数量归零时触发。 |
| 用户解锁 | `screen_unlock` | 收到 `Intent.ACTION_USER_PRESENT` 时触发。 |
| 充电开始/结束 | `battery_change` | 收到 `ACTION_POWER_CONNECTED` 或 `ACTION_POWER_DISCONNECTED` 时触发。 |
| 安装应用 | `install_app` | 收到 `ACTION_PACKAGE_ADDED` 时触发。 |
| 卸载应用 | `uninstall_app` | 收到 `ACTION_PACKAGE_REMOVED` 时触发。 |
| 亮屏 | `screen_on` | 可配置 `configs` 子项，用 `delay` 做延迟通知。 |

通知点击会启动宿主 launcher Activity，并携带以下 extras：

- `AppOpenFrom`：打开来源。FCM 通知通常由服务端透传或使用 `Push`，SDK 临时通知使用 `app_push`，常驻通知使用 `persistent`。
- `Route`：后端/配置映射后的路由。
- `Scene`：触发场景。
- `NoticeId`、`DistinctId`：推送点击埋点参数。

宿主 Activity 需要在 `onCreate` 和 `onNewIntent` 中读取 extras，并按自己的导航系统处理 route：

```kotlin
private fun handleLaunchParams(intent: Intent) {
    val appOpenFrom = intent.getStringExtra("AppOpenFrom").orEmpty()
    val route = intent.getStringExtra("Route").orEmpty()
    val scene = intent.getStringExtra("Scene").orEmpty()

    AdvertiseSdk.setSuperProperties(
        mapOf("traffic_source" to if (appOpenFrom == "Push") "fcm_push" else appOpenFrom)
    )

    if (scene.isNotEmpty()) {
        AdvertiseSdk.logEvent("notification_app_clicked", mapOf("scene" to scene))
    }
}
```

常驻通知服务可在 Activity resume 时兜底拉起：

```kotlin
AdvertiseSdk.ensurePersistentNotificationServiceRunning(this)
```

调试通知：

```kotlin
AdvertiseSdk.sendDebugNotification(
    context = context,
    notificationType = "screen_unlock",
    configName = "screen_unlock",
)
```

## 10. 埋点与用户属性

普通事件：

```kotlin
AdvertiseSdk.logEvent(
    eventName = "enter_homepage",
    params = mapOf("channel" to "default"),
)
```

ThinkingData 用户属性：

```kotlin
AdvertiseSdk.setUserOnceAttr(
    AdvertiseSdk.ThinkingKeys.firstOpenTime,
    AdvertiseSdk.getFirstOpenTime(),
)

AdvertiseSdk.setUserAttr(
    AdvertiseSdk.ThinkingKeys.latestOpenTime,
    AdvertiseSdk.getLatestOpenTime(),
)
```

公共属性：

```kotlin
AdvertiseSdk.setSuperProperties(
    mapOf("traffic_source" to "fcm_push")
)
```

调试信息：

```kotlin
val deviceId = AdvertiseSdk.getThinkingDeviceId()
val topic = AdvertiseSdk.topic
val isNature = AdvertiseSdk.isNature
```

## 11. Remote Config 约定

启用 `remoteConfig(enabled = true)` 后，SDK 会读取以下常用参数：

| 参数 | 作用 |
| --- | --- |
| `ABTestName` | 写入 ThinkingData 用户属性。 |
| `update_version` | 强更目标 versionCode。 |
| `OpenAdmobMediation` | 是否启用 AdMob mediation adapter 初始化。 |
| `Admob_Banner`/`Admob_Interset`/`Admob_Native`/`Admob_Open` | 覆盖 AdMob 广告位 ID。 |
| `ad_mapping` | 按自然量、国家、品牌选择不同广告/通知/预加载配置与 FCM topic。 |
| `ad_policy` | 默认广告策略。 |
| `adload_config` | 预加载策略。 |
| `notification_config` | 通知触发策略。 |
| `notification_content` | 通知展示内容。 |
| `native_ad_ids` | Native 高/中/低价广告 ID 队列。 |
| `native_ad_policy` | Native 广告位策略。 |
| `native_refresh_time` | Native 自动刷新间隔，单位秒。 |
| `guide_page_swap_time` | 引导页自动切换间隔，单位毫秒。 |
| `log_time` | 日志相关时间窗口。 |

`ad_mapping` 会在 Singular 归因结果可用后才允许按自然量、国家、品牌做定向选择；否则使用默认配置，避免归因未返回时切错策略。

### 11.1 Remote Config JSON 示例

`adload_config` 示例：

```json
{
  "adload_cache_time": 3600,
  "adload_retry_num": 1,
  "adload_max_time": 6,
  "adload_poolsize_open": 1,
  "adload_poolsize_inter": 1,
  "adload_poolsize_native": 3,
  "adload_trigger_timing_open": {
    "open_app": true,
    "play_finish": true,
    "enter_background": false,
    "receive_notification": false
  },
  "adload_trigger_timing_inter": {
    "open_app": false,
    "play_finish": true,
    "enter_background": false,
    "receive_notification": false,
    "enter_features": true
  },
  "adload_trigger_timing_native": {
    "open_app": true,
    "play_finish": true,
    "enter_background": false,
    "receive_notification": false,
    "enter_features": true
  }
}
```

时间字段说明：`adload_cache_time` 和 `adload_max_time` 在后台配置中按秒填写，SDK 内部会转换成毫秒。

`native_ad_policy` 示例：

```json
{
  "limited": 50,
  "limited_loadtime_seconds": 86400,
  "totalClickMaxPerDay": 20,
  "totalRequestMaxPerDay": 200,
  "ad_units": [
    {
      "areakey": "homeNativeAdv",
      "ad_format": "native",
      "rate": 1.0,
      "frequency_caps": {
        "max_per_hour": 4,
        "max_per_day": 20,
        "interval_seconds": 15
      }
    }
  ]
}
```

`native_ad_ids` 示例：

```json
[
  {
    "highPriceID": "ca-app-pub-xxx/native-high",
    "midPriceID": "ca-app-pub-xxx/native-mid",
    "lowPriceID": "ca-app-pub-xxx/native-low"
  }
]
```

Debug 模式下 `AdvIDs.setNativeIDs()` 会优先使用 `AdMobConfig.debugNativeIdsJson`，方便避免误请求线上 Native ID。

`ad_mapping` 示例：

```json
{
  "config": {
    "ad": "ad_policy",
    "notification": "notification_config",
    "fcm_topic": "all",
    "preload": "adload_config"
  },
  "nature_config": {
    "ad": "ad_policy_nature",
    "notification": "notification_config_nature",
    "fcm_topic": "all_nature",
    "preload": "adload_config_nature"
  },
  "configs": [
    {
      "countrys": ["kr", "jp"],
      "brands": ["samsung", "oppo"],
      "config": {
        "ad": "ad_policy_asia",
        "notification": "notification_config_asia",
        "fcm_topic": "asia",
        "preload": "adload_config_asia"
      }
    }
  ]
}
```

选择规则：

- `nature_config`：当自然量/Google IP/付费用户等条件命中时使用。
- `configs`：按 `Locale.getDefault().country` 和 `Build.BRAND` 同时匹配。
- 某个 tag 对应的 Remote Config 值为空时，会按字段回退到默认 `ad_policy`、`notification_config`、`adload_config`。

`notification_config` 示例：

```json
{
  "24HMax": 8,
  "each_trigger_sent": 1,
  "NMax": 3,
  "has_sound_alert": true,
  "has_vibration": true,
  "is_foreground_send": false,
  "content": "",
  "triggers": [
    {
      "id": "1",
      "name": "screen_unlock",
      "offset_second": 21600,
      "interval_second": 960
    },
    {
      "id": "2",
      "name": "screen_on",
      "configs": [
        {
          "id": "2-1",
          "name": "screen_on_5s",
          "delay": 5,
          "offset_second": 21600,
          "interval_second": 900
        }
      ]
    }
  ],
  "timer": [
    {
      "id": "daily_1",
      "name": "daily_notification",
      "HH": 9,
      "MM": 30
    }
  ]
}
```

`notification_content` 示例：

```json
[
  {
    "Id": 1,
    "AppName": "Host App",
    "AppPackage": "com.example.app",
    "Policy": 1,
    "NoticeId": "notice-001",
    "Title": "Recover your files",
    "Content": "Tap to continue recovery.",
    "Button": "Open",
    "Icon": "",
    "Img": "",
    "Languages": "{\"keys\":[{\"language\":\"es\",\"title\":\"Recupera tus archivos\",\"content\":\"Toca para continuar.\",\"img\":\"\",\"button\":\"Abrir\"}]}",
    "Route": "/recoverFiles"
  }
]
```

`Route` 会先通过 `NotificationFeatureConfig.routeMappings` 映射成宿主路由；未配置映射时按原始 route 传给宿主 Activity。

## 12. 常用状态读取

```kotlin
AdvertiseSdk.privacyUrl
AdvertiseSdk.termsUrl
AdvertiseSdk.shouldIgnoreGuide
AdvertiseSdk.hasOpenLaunchPage
AdvertiseSdk.isFirstOpenAdEnabled
AdvertiseSdk.isGoogleIp
AdvertiseSdk.isPaidUser
AdvertiseSdk.shouldSuppressAdsForCurrentUser
AdvertiseSdk.isNature
AdvertiseSdk.topic
```

其中 `shouldSuppressAdsForCurrentUser` 当前等价于 `isPaidUser || isGoogleIp`，宿主在非 Debug 场景可以据此隐藏广告 UI 或跳过广告流程。

## 13. 本地存储工具

SDK 暴露了简单字符串偏好读写，便于宿主共享 SDK 内的 Preference 存储：

```kotlin
AdvertiseSdk.putPreferenceString("key", "value")
val value = AdvertiseSdk.getPreferenceString("key", "default")
```

## 14. 调试排查与验证清单

### 14.1 调试缓存键

Debug 模式下，SDK 会把部分远程配置和路由结果写入 Preference，宿主可以通过 `AdvertiseSdk.getPreferenceString()` 或调试页面读取。

| Key | 内容 |
| --- | --- |
| `ad_mapping` | Remote Config 原始 `ad_mapping`。 |
| `preloadTag` | 当前命中的预加载配置 tag。 |
| `adTag` | 当前命中的广告策略 tag。 |
| `notificationTag` | 当前命中的通知配置 tag。 |
| `routing_source` | 当前路由应用来源，例如 `RemoteConfig.update` 或 `Ads.initSingular`。 |
| `updateConfigFromJson` | 最近一次应用的 `adload_config`。 |
| `setPolicyFromJson` | 最近一次应用的 `ad_policy`。 |
| `notification_config` | 当前通知触发策略。 |
| `notification_content` | 当前通知展示内容。 |
| `Contextualized_Push` | Remote Config 中的上下文化推送原始值。 |
| `openReview` | Remote Config 中的 review 开关。 |

常用调试读取示例：

```kotlin
val adPolicy = AdvertiseSdk.getPreferenceString("setPolicyFromJson", "")
val preloadConfig = AdvertiseSdk.getPreferenceString("updateConfigFromJson", "")
val routingSource = AdvertiseSdk.getPreferenceString("routing_source", "")
```

### 14.2 排查顺序

广告不展示时按这个顺序查：

1. `AdvertiseSdk.init` 是否执行，`adMob.enabled` 是否为 true。
2. Manifest 中 AdMob App ID 是否和 `AdvertiseSdkConfig.adMob.appId` 一致。
3. 当前是否 Debug；Debug 下是否使用测试 ID 或测试设备。
4. 当前用户是否命中 `shouldSuppressAdsForCurrentUser`。
5. `areaKey` 是否存在于 `ad_policy` 或 `native_ad_policy`。
6. `global_ad_switch`、`rate`、`frequency_caps` 是否允许展示。
7. 预加载场景是否被 `adload_config` 打开。
8. Remote Config 是否覆盖了本地策略，且覆盖内容是否为空。
9. Native 是否同时有 `native_ad_policy` 和 `native_ad_ids`。
10. 设备网络、Google Play 服务、广告请求日志是否正常。

通知不触发时按这个顺序查：

1. Android 13+ 是否已授予通知权限。
2. `notifications.enabled` 和 `push.enabled` 是否为 true。
3. SDK Manifest 组件是否存在且为固定 enabled。
4. `notification_config` 是否下发，trigger name 是否匹配 SDK 内部触发名。
5. `push.json` 的 `first_trigger_time`，以及 `notification_config` 的 `offset_second`、`interval_second`、`24HMax` 是否限制了发送。
6. App 当前在前台时，`is_foreground_send` 是否允许发送。
7. `notification_content` 是否下发，`Route` 是否能映射到宿主页面。

### 14.3 验证清单

接入后建议按以下顺序验证：

1. App 启动不崩溃，`AdvertiseSdk.init` 完成。
2. Debug 下 AdMob App ID 与 manifest meta-data 一致。
3. `BuildConfig.DEBUG` 时使用测试广告位或 Debug 配置。
4. `ad_policy.json` 中使用的 `areakey` 与页面传入字符串完全一致。
5. 插屏 `onClosed` 能继续业务流程。
6. Banner dispose 后没有重复持有旧 `AdView`。
7. Native 已下发 `native_ad_policy` 和 `native_ad_ids`，页面能拿到 `NativeAd`。
8. Android 13+ 通知权限已申请，推送/常驻通知配置与 SDK 固定 enabled 组件链路可用。
9. 通知点击能把 `Route`、`Scene`、`AppOpenFrom` 传到宿主 Activity。
10. Release 包签名已加入 `safe.expectedSignatures`，且 Debug 不会误触发强杀。
11. Remote Config 断网或空配置时，本地 `ad_policy.json` 仍能兜底控制开屏、插屏和 Banner。

### 14.4 发布前检查

发布前至少确认以下项：

| 检查项 | 通过标准 |
| --- | --- |
| 包名 | `applicationId`、Firebase、AdMob、Facebook、TikTok、后端配置一致 |
| AdMob App ID | Manifest meta-data 和 `AdvertiseSdkConfig.adMob.appId` 一致 |
| 广告位 ID | Debug 不请求线上广告位，Release 不误用测试广告位 |
| 策略文件 | `ad_policy.json` 的 `package_name` 为空或等于 Release 包名 |
| Remote Config | 默认 key、定向 key、空值回退均已验证 |
| Native 策略 | `native_ad_policy` 覆盖所有 Native areaKey |
| 通知权限 | Android 13+ 首次触发通知前已经有授权流程 |
| SDK 组件 | Manifest 合并产物中 SDK service、receiver、provider 均为 `android:enabled="true"` |
| 安全校验 | Release 签名 SHA-256 已写入，灰度包签名也在允许列表 |
| Play Integrity | Google Cloud project number 和服务端解析密钥可用 |
| 混淆 | Release 包安装启动、广告展示、通知点击、Remote Config 均已验证 |

## 15. 常见问题

### 初始化时报 AdMob App ID 不一致

检查 `manifestPlaceholders["advAdmobAppId"]` 和 `adMob(appId = ...)` 是否使用同一个值。Debug 下 SDK 会直接 `error`，方便尽早发现配置漂移。

### Native 广告一直不展示

优先检查 Remote Config 是否下发 `native_ad_policy`，并确认其中的 `areakey` 与 `rememberNativeAd(areaKey = ...)` 一致。普通 Native 广告位没有本地默认策略兜底。

### 插屏没有展示但业务继续了

这是预期行为。未启用广告网络、策略过滤、频控命中、广告加载失败时，SDK 会执行关闭回调，让宿主业务继续。

### Release 中自然量或 Google IP 用户没有广告

当前 SDK 在非测试环境会对 `paid_0` 或 `isGoogleIP` 用户屏蔽 Banner/Native，并通过 `shouldSuppressAdsForCurrentUser` 暴露给宿主。宿主可根据该状态隐藏广告容器。

### 推送组件打开后仍收不到通知

检查四项是否同时满足：Firebase Messaging 配置、SDK service 在 merged manifest 中为 enabled、`google-services.json`、通知运行时权限。还要确认当前 FCM topic 与后台发送 topic 一致。

### Debug 能展示广告，Release 不展示

优先检查 Release 包使用的真实广告位 ID、`ad_policy.package_name`、安全校验签名、Remote Config 默认值。Release 下还会屏蔽 `paid_0` 或 `isGoogleIP` 用户的 Banner/Native。

### 通知点击后没有进入目标页面

确认 `NotificationFeatureConfig.routeMappings` 是否把后端 `Route` 映射成宿主导航可识别的 route，并确认宿主 Activity 在 `onCreate` 和 `onNewIntent` 都调用了参数解析逻辑。`MainActivity` 使用 `singleTask` 时，后续通知点击通常走 `onNewIntent`。

### 配置开关已经打开但组件没有生效

检查 SDK 组件是否存在于 merged manifest 且为 `android:enabled="true"`，再检查 Firebase、通知权限、Remote Config、本地 `push.json` 和宿主路由配置是否完整。

### Remote Config 下发后仍使用默认策略

检查 `ad_mapping` 是否为空、Singular 归因结果是否已返回、定向 tag 对应的 Remote Config 值是否为空。定向选择未满足时会回退默认 `ad_policy`、`notification_config` 和 `adload_config`。
