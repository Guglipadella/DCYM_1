package it.polito.did.dcym.ui.theme
import it.polito.did.dcym.ui.components.GraphPaperBackground

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.VioletIntense,
    onPrimary = AppColors.White,

    secondary = AppColors.YellowPastel,
    onSecondary = AppColors.MagentaDark,

    tertiary = AppColors.OrangePastelMuted,
    onTertiary = AppColors.MagentaDark,

    background = AppColors.MagentaDark,
    onBackground = AppColors.White,

    surface = Color(0xFF3A1026), // un “surface” scuro coerente col magenta
    onSurface = AppColors.White,

    outline = AppColors.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.VioletIntense,
    onPrimary = AppColors.White,

    secondary = AppColors.YellowPastel,
    onSecondary = AppColors.MagentaDark,

    tertiary = AppColors.OrangePastelMuted,
    onTertiary = AppColors.MagentaDark,

    background = AppColors.PaperBg,
    onBackground = AppColors.MagentaDark,

    surface = AppColors.White,
    onSurface = AppColors.MagentaDark,

    outline = AppColors.MagentaDark
)

@Composable
fun DontCallYourMomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Se vuoi palette fissa e coerente con le reference, lascia false di default:
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        GraphPaperBackground {
            content()
        }
    }

}
