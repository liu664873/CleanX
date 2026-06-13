package com.quickcleanpro.phonecleaner.domain.repository

import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import com.quickcleanpro.phonecleaner.domain.model.toolbox.AppUsageInfo
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkScanResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedProgress
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkSpeedResult
import com.quickcleanpro.phonecleaner.domain.model.toolbox.NetworkUsageInfo

/**
 * 工具箱功能仓库接口。
 *
 * 该接口聚合了应用使用情况统计（App Usage）、网络工具（Network）、
 * 通知拦截器（Notification）等工具箱相关功能的数据访问与系统跳转能力。
 * 通过此接口，上层（ViewModel / UseCase）可以统一调用，而不必依赖多个独立的 Repository。
 */
interface ToolboxRepository {
    // -------------------- 应用使用情况（App Usage） --------------------

    /**
     * 判断是否已授予应用使用情况访问权限（PACKAGE_USAGE_STATS）。
     */
    fun hasAppUsageAccess(): Boolean

    /**
     * 清除使用情况访问权限的缓存状态。
     * 通常在用户从系统设置页返回后调用，以确保获取最新的权限状态。
     */
    fun resetAppUsagePermissionCache()

    /**
     * 返回跳转到“有权查看使用情况的应用”系统设置页的 Intent。
     */
    fun appUsageSettingsIntent(): Intent

    /**
     * 返回跳转到指定应用详情页的 Intent（用于强制停止、卸载等操作）。
     *
     * @param packageName 目标应用的包名
     */
    fun appInfoIntent(packageName: String): Intent

    /**
     * 读取指定时间范围内的应用使用情况统计。
     *
     * @param startMillis 起始时间戳（毫秒）
     * @param endMillis   结束时间戳（毫秒）
     * @return 应用使用信息列表
     */
    suspend fun appUsageBetween(
        startMillis: Long,
        endMillis: Long,
    ): List<AppUsageInfo>

    /**
     * 判断指定包名集合中哪些应用当前正在运行（或最近活跃）。
     *
     * @param packageNames 待检查的包名集合
     * @return 正在运行的应用包名集合
     */
    suspend fun runningPackages(packageNames: Set<String>): Set<String>

    // -------------------- 网络工具（Network） --------------------

    /**
     * 判断当前设备是否已连接网络（Wi-Fi 或移动数据）。
     */
    fun isNetworkAvailable(): Boolean

    /**
     * 判断当前是否已连接 Wi-Fi。
     */
    fun isWifiConnected(): Boolean

    /**
     * 判断当前是否已连接移动网络（蜂窝数据）。
     */
    fun isMobileConnected(): Boolean

    /**
     * 判断是否已授予网络使用情况统计所需的权限（ACCESS_NETWORK_STATE 等）。
     */
    fun hasNetworkUsageAccess(): Boolean

    /**
     * 返回跳转到网络使用情况统计设置页的 Intent。
     */
    fun networkUsageSettingsIntent(): Intent

    /**
     * 读取今日的网络使用情况（Wi-Fi 和移动数据的收发字节数）。
     */
    suspend fun readNetworkUsage(): NetworkUsageInfo

    /**
     * 执行网络速度测试（下载/上传速度、延迟）。
     */
    suspend fun runSpeedTest(): NetworkSpeedResult

    /**
     * 执行带进度回调的网络速度测试。
     *
     * @param onProgress 进度回调，用于实时更新 UI（当前速度、阶段等）
     */
    suspend fun runSpeedTestWithProgress(onProgress: (NetworkSpeedProgress) -> Unit): NetworkSpeedResult

    /**
     * 扫描当前 Wi-Fi 网络中的设备（IP、MAC、主机名等）。
     */
    suspend fun scanWifi(): NetworkScanResult

    // -------------------- 通知拦截器（Notification） --------------------

    /**
     * 判断是否已授予通知监听权限（NotificationListenerService）。
     */
    fun hasNotificationListenerAccess(): Boolean

    /**
     * 判断通知拦截功能是否已开启。
     */
    fun isNotificationBlockingEnabled(): Boolean

    /**
     * 设置通知拦截功能的开关状态。
     *
     * @param enabled true 表示开启拦截，false 表示关闭
     */
    fun setNotificationBlockingEnabled(enabled: Boolean)

    /**
     * 获取累计拦截的通知总数。
     */
    fun blockedNotificationCount(): Int

    /**
     * 获取按应用包名分组的累计拦截通知数量。
     */
    fun blockedNotificationCountsByPackage(): Map<String, Int>

    /**
     * 获取用户已选择拦截的应用包名集合。
     */
    fun selectedNotificationPackages(): Set<String>

    /**
     * 获取可展示在通知拦截设置页中的应用列表（包含包名、应用名、图标等）。
     */
    fun notificationApps(): List<BlockableNotificationApp>

    /**
     * 设置指定应用是否被选中为通知拦截对象。
     *
     * @param packageName 应用包名
     * @param selected    true 表示选中（拦截其通知），false 表示取消选中
     */
    fun setNotificationPackageSelected(
        packageName: String,
        selected: Boolean,
    )

    /**
     * 返回跳转到通知监听权限设置页的 Intent。
     */
    fun notificationListenerSettingsIntent(): Intent

    /**
     * 返回跳转到指定应用的通知设置页的 Intent（用于管理单个应用的通知渠道）。
     *
     * @param packageName 应用包名
     */
    fun appNotificationSettingsIntent(packageName: String): Intent

    /**
     * 返回跳转到指定应用详情页的 Intent（作为通知设置不可用时的备选入口）。
     *
     * @param packageName 应用包名
     */
    fun appDetailsSettingsIntent(packageName: String): Intent
}
