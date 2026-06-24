package com.quickcleanpro.phonecleaner.presentation.screen.applock.views

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXPrimaryButton
import java.util.concurrent.ConcurrentHashMap

@Composable
internal fun AppLockCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            content = content
        )
    }
}

@Composable
internal fun AppLockDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppLockDividerColor)
    )
}

@Composable
internal fun AppLockBottomBar(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppLockBackground)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        CleanXPrimaryButton(
            text = text,
            onClick = onClick,
            enabled = enabled,
            height = 52.dp,
            cornerRadius = 10.dp,
            fontSize = 20.sp
        )
    }
}

@Composable
internal fun LoadingCard() {
    AppLockCard {
        LoadingState()
    }
}

@Composable
internal fun EmptyCard(text: String) {
    AppLockCard {
        EmptyState(text = text)
    }
}

@Composable
internal fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = CleanXBlue)
    }
}

@Composable
internal fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = AppLockSecondaryText, fontSize = 16.sp)
    }
}

@Composable
internal fun PackageAppIcon(
    packageName: String,
    fallbackText: String
) {
    val context = LocalContext.current
    val bitmap = remember(packageName) { loadPackageIconBitmap(context, packageName) }
    if (bitmap != null) {
        Image(
            painter = BitmapPainter(bitmap),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CleanXBlue.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackText,
                color = CleanXBlue,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val appLockIconCache = ConcurrentHashMap<String, ImageBitmap>()

private fun loadPackageIconBitmap(context: android.content.Context, packageName: String): ImageBitmap? {
    appLockIconCache[packageName]?.let { return it }
    val drawable = runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
        ?: return null
    val bitmap = drawable.safeBitmap().asImageBitmap()
    appLockIconCache[packageName] = bitmap
    return bitmap
}

private fun Drawable.safeBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    return toBitmap(width = 96, height = 96)
}
