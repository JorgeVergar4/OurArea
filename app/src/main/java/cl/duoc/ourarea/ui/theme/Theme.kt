package cl.duoc.ourarea.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.TextOnPrimary,
    primaryContainer = AppColors.GoogleBlue.copy(alpha = 0.1f),
    onPrimaryContainer = AppColors.PrimaryDark,

    secondary = AppColors.Secondary,
    onSecondary = AppColors.TextOnPrimary,
    secondaryContainer = AppColors.GoogleGreen.copy(alpha = 0.1f),
    onSecondaryContainer = AppColors.SecondaryVariant,

    tertiary = AppColors.GoogleYellow,
    onTertiary = AppColors.TextPrimary,

    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,

    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,

    surfaceVariant = AppColors.BackgroundGray,
    onSurfaceVariant = AppColors.TextSecondary,

    error = AppColors.Error,
    onError = AppColors.TextOnPrimary,

    outline = AppColors.BorderLight,
    outlineVariant = AppColors.Divider
)

@Composable
fun OurAreaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

