package sasuke.pinterestdl.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YtdlnisDarkColorScheme = darkColorScheme(
    primary = AccentWhite,
    onPrimary = Color.Black,
    primaryContainer = DarkPillHighlight,
    onPrimaryContainer = Color.White,
    secondary = DarkPillHighlight,
    onSecondary = Color.White,
    background = PitchBlack,
    onBackground = TextWhite,
    surface = DarkGreySurface,
    onSurface = TextWhite,
    surfaceVariant = DarkGreySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkPillHighlight
)

@Composable
fun PinterestDLTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YtdlnisDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PinterestDLTheme(content = content)
}
