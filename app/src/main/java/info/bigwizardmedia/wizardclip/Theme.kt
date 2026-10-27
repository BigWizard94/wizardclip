package info.bigwizardmedia.wizardclip

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF6D28D9)
private val PurpleDark = Color(0xFF7C3AED)
private val Teal = Color(0xFF14B8A6)
private val TealDark = Color(0xFF2DD4BF)

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF3B0F7A),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = Color(0xFF0F766E),
    background = Color(0xFFFAF9FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1EEFB),
    onSurfaceVariant = Color(0xFF4B4458),
)

private val DarkColors = darkColorScheme(
    primary = PurpleDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B1A7A),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = TealDark,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = TealDark,
    background = Color(0xFF12101A),
    surface = Color(0xFF1B1826),
    surfaceVariant = Color(0xFF262238),
    onSurfaceVariant = Color(0xFFCFC8E0),
)

@Composable
fun WizardClipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
