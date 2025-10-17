package cl.duoc.ourarea.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.LightTeal,
    onPrimaryContainer = AppColors.PrimaryDark,

    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = AppColors.LightTeal,
    onSecondaryContainer = AppColors.PrimaryDark,

    tertiary = AppColors.MediumTeal,
    onTertiary = Color.White,

    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,

    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,

    surfaceVariant = AppColors.BackgroundGray,
    onSurfaceVariant = AppColors.TextSecondary,

    error = AppColors.Error,
    onError = Color.White,

    outline = AppColors.BorderLight,
    outlineVariant = AppColors.Divider
)

