package ke.co.smartroundclinic.patient.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val ColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,

    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary             = Tertiary,
    onTertiary           = OnTertiary,
    tertiaryContainer    = TertiaryContainer,
    onTertiaryContainer  = OnTertiaryContainer,

    background           = Background,
    onBackground         = OnBackground,
    surface              = Surface,
    onSurface            = OnSurface,
    surfaceVariant       = SurfaceVariant,
    onSurfaceVariant     = OnSurfaceVariant,
    outline              = Outline,
    outlineVariant       = OutlineVariant,

    error                = Error,
    onError              = OnError,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer,
)

data class SmartRoundColors(
    val gradientStart   : Color = GradientStart,
    val gradientEnd     : Color = GradientEnd,
    val buttonGradientStart : Color = ButtonGradientStart,
    val buttonGradientEnd : Color = ButtonGradientEnd,
    val topAppBarGradientStart: Color = TopAppBarGradientStart,
    val topAppBarGradientEnd: Color = TopAppBarGradientEnd,
    val cardBackground  : Color = CardBackground,
    val cardAccent      : Color = CardAccent,
    val searchOverlay   : Color = SearchBarOverlay,
    val statusPublished : Color = StatusPublished,
    val statusSuspended : Color = StatusSuspended,
    val statusPending   : Color = StatusPending,
    val statusConfirmed : Color = StatusConfirmed,
)

val LocalSmartRoundColors = staticCompositionLocalOf { SmartRoundColors() }

val MaterialTheme.smartRoundColors: SmartRoundColors
    @Composable get() = LocalSmartRoundColors.current

@Composable
fun SmartRoundTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSmartRoundColors provides SmartRoundColors()) {
        MaterialTheme(
            colorScheme = ColorScheme,
            typography  = SmartRoundTypography,
            shapes      = SmartRoundShapes,
            content     = content,
        )
    }
}
