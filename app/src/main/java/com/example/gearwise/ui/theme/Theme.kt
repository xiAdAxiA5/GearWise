package com.example.gearwise.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Charcoal,
    onPrimary = CardWhite,
    primaryContainer = SurfaceWarm,
    onPrimaryContainer = Charcoal,
    secondary = MidGray,
    onSecondary = CardWhite,
    secondaryContainer = SurfaceWarm,
    onSecondaryContainer = Charcoal,
    background = PaperWhite,
    onBackground = Charcoal,
    surface = CardWhite,
    onSurface = Charcoal,
    surfaceVariant = SurfaceWarm,
    onSurfaceVariant = MidGray,
    outline = WarmBorder,
    outlineVariant = WarmBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkText,
    onPrimary = DarkBg,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkText,
    secondary = DarkSecondary,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkText,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceWarm,
    onSurfaceVariant = DarkSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

@Composable
fun GearWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = GearWiseShapes,
        content = content
    )
}

/** 极简形状：小圆角，干净利落 */
private val GearWiseShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
)
