package ke.co.smartroundclinic.patient.presentation.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import ke.co.smartroundclinic.patient.generated.resources.Res
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ke.co.smartroundclinic.patient.generated.resources.montserrat_bold
import ke.co.smartroundclinic.patient.generated.resources.montserrat_medium
import ke.co.smartroundclinic.patient.generated.resources.montserrat_regular
import ke.co.smartroundclinic.patient.generated.resources.montserrat_semi_bold
import ke.co.smartroundclinic.patient.generated.resources.raleway_bold
import ke.co.smartroundclinic.patient.generated.resources.raleway_medium
import ke.co.smartroundclinic.patient.generated.resources.raleway_regular
import ke.co.smartroundclinic.patient.generated.resources.raleway_semi_bold
import org.jetbrains.compose.resources.Font

val Raleway: FontFamily
    @Composable get() = FontFamily(
        Font(resource = Res.font.raleway_regular,   weight = FontWeight.Normal),
        Font(resource = Res.font.raleway_medium,    weight = FontWeight.Medium),
        Font(resource = Res.font.raleway_semi_bold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.raleway_bold,      weight = FontWeight.Bold),
    )

val Montserrat: FontFamily
    @Composable get() = FontFamily(
        Font(resource = Res.font.montserrat_regular,   weight = FontWeight.Normal),
        Font(resource = Res.font.montserrat_medium,    weight = FontWeight.Medium),
        Font(resource = Res.font.montserrat_semi_bold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.montserrat_bold,      weight = FontWeight.Bold),
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val SmartRoundTypography: Typography
    @Composable get() = Typography(
        displayLarge = TextStyle(
            fontFamily    = Raleway,
            fontWeight    = FontWeight.Bold,
            fontSize      = 57.sp,
            lineHeight    = 64.sp,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium = TextStyle(
            fontFamily    = Raleway,
            fontWeight    = FontWeight.Bold,
            fontSize      = 45.sp,
            lineHeight    = 52.sp,
            letterSpacing = 0.sp,
        ),
        displaySmall = TextStyle(
            fontFamily    = Raleway,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 36.sp,
            lineHeight    = 44.sp,
            letterSpacing = 0.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily    = Raleway,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 32.sp,
            lineHeight    = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily    = Raleway,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 28.sp,
            lineHeight    = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily    = Raleway,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 24.sp,
            lineHeight    = 32.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily    = Raleway,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 22.sp,
            lineHeight    = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Medium,
            fontSize      = 16.sp,
            lineHeight    = 24.sp,
            letterSpacing = 0.15.sp,
        ),
        titleSmall = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Medium,
            fontSize      = 14.sp,
            lineHeight    = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Normal,
            fontSize      = 16.sp,
            lineHeight    = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Normal,
            fontSize      = 14.sp,
            lineHeight    = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Normal,
            fontSize      = 12.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        bodySmallEmphasized = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Normal,
            fontSize      = 8.sp,
            lineHeight    = 10.sp,
            letterSpacing = 0.4.sp,
        ),
        labelLarge = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.SemiBold,
            fontSize      = 14.sp,
            lineHeight    = 20.sp,
            letterSpacing = 1.25.sp,
        ),
        labelMedium = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Medium,
            fontSize      = 12.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily    = Montserrat,
            fontWeight    = FontWeight.Medium,
            fontSize      = 11.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.8.sp,
        ),
    )
