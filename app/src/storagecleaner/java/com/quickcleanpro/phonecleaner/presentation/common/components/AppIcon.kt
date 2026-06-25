package com.quickcleanpro.phonecleaner.presentation.common.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import java.util.concurrent.ConcurrentHashMap

/**
 * 应用图标内存缓存，避免重复加载
 */
private val packageIconBitmapCache = ConcurrentHashMap<String, ImageBitmap>()

/**
 * 根据包名加载应用图标，返回 ImageBitmap，失败返回 null
 * 加载结果会缓存在内存中
 */
private fun loadPackageIconBitmap(context: Context, packageName: String): ImageBitmap? {
    packageIconBitmapCache[packageName]?.let { return it }

    val packageManager = context.packageManager
    // 尝试多种方式获取图标
    val icon = runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()
        ?: runCatching {
            packageManager.getLaunchIntentForPackage(packageName)
                ?.resolveActivity(packageManager)
                ?.let { componentName ->
                    packageManager.getActivityInfo(componentName, 0).loadIcon(packageManager)
                }
        }.getOrNull()
        ?: runCatching {
            packageManager.getApplicationInfo(packageName, 0).loadIcon(packageManager)
        }.getOrNull()

    return icon?.toBitmap()?.asImageBitmap()?.also {
        packageIconBitmapCache[packageName] = it
    }
}

/**
 * 将 Drawable 转换为 Bitmap
 */
private fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val width = intrinsicWidth.takeIf { it > 0 } ?: 96
    val height = intrinsicHeight.takeIf { it > 0 } ?: 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

/**
 * 纯文字图标（无包名加载），用于备选或聚合项
 * @param text 显示的文字（通常为首字母）
 * @param color 背景颜色
 * @param modifier 修饰符（可控制尺寸等）
 */
@Composable
fun TextIcon(
    text: String,
    color: Color,
    modifier: Modifier = Modifier.size(44.dp)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 通用应用图标组件，优先使用真实应用图标，否则显示备选占位
 * @param packageName 应用包名（可为空，空时直接显示占位）
 * @param fallbackText 备选文字（通常为首字母），当无法获取图标时显示
 * @param color 备选文字的背景色（仅当显示文字时使用）
 * @param isAggregate 是否为聚合项（如“系统与未知流量”），此时直接显示备选文字
 * @param modifier 外部修饰符，可调整尺寸等
 */
@Composable
fun PackageAppIcon(
    packageName: String?,
    fallbackText: String,
    color: Color = Color(0xFF000000),
    isAggregate: Boolean = false,
    modifier: Modifier = Modifier.size(44.dp)
) {
    if (isAggregate) {
        TextIcon(text = fallbackText, color = color, modifier = modifier)
        return
    }

    val context = LocalContext.current
    val appIcon = remember(packageName) {
        packageName?.let { loadPackageIconBitmap(context, it) }
    }

    Box(
        modifier = modifier.size(44.dp).clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // 备选：显示通用占位图标
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * 清理图标缓存（可选，在应用卸载或需要刷新时调用）
 */
fun clearPackageIconCache() {
    packageIconBitmapCache.clear()
}