package ke.co.smartroundclinic.patient.presentation.theme

import androidx.compose.ui.graphics.Color

val Primary0   = Color(0xFF000000)
val Primary10  = Color(0xFF1A0500)
val Primary20  = Color(0xFF7A1F00)
val Primary30  = Color(0xFFB83200)
val Primary40  = Color(0xFFE84E1C)
val Primary70  = Color(0xFFFF8A65)
val Primary80  = Color(0xFFFFB59B)
val Primary90  = Color(0xFFFFDBD0)
val Primary95  = Color(0xFFFFEDE8)
val Primary99  = Color(0xFFFFF8F6)
val Primary100 = Color(0xFFFFFFFF)

val Secondary10  = Color(0xFF2C1512)
val Secondary20  = Color(0xFF4E2523)
val Secondary30  = Color(0xFF6E3E3B)
val Secondary40  = Color(0xFF8F5A57)
val Secondary80  = Color(0xFFD9B9B5)
val Secondary90  = Color(0xFFF5DDD9)
val Secondary95  = Color(0xFFFAEEEB)
val Secondary99  = Color(0xFFFFF8F6)

val Tertiary10  = Color(0xFF00201E)
val Tertiary20  = Color(0xFF004845)
val Tertiary30  = Color(0xFF006B67)
val Tertiary40  = Color(0xFF1B8F8A)
val Tertiary80  = Color(0xFF8DD8D3)
val Tertiary90  = Color(0xFFC8F0EC)
val Tertiary99  = Color(0xFFF4FBFA)

val Neutral0  = Color(0xFF000000)
val Neutral10 = Color(0xFF1C1B1B)
val Neutral20 = Color(0xFF393938)
val Neutral40 = Color(0xFF5C5A59)
val Neutral60 = Color(0xFF939190)
val Neutral80 = Color(0xFFCAC5C3)
val Neutral90 = Color(0xFFE8E3E1)
val Neutral95 = Color(0xFFF5F0EE)
val Neutral99 = Color(0xFFFFFBFF)
val Neutral100 = Color(0xFFFFFFFF)

val Error10  = Color(0xFF410002)
val Error40  = Color(0xFFDD2013)
val Error90  = Color(0xFFFFE8E6)

val Primary            = Primary40
val OnPrimary          = Neutral100
val PrimaryContainer   = Primary90
val OnPrimaryContainer = Primary10

val Secondary            = Secondary40
val OnSecondary          = Neutral100
val SecondaryContainer   = Secondary90
val OnSecondaryContainer = Secondary10

val Tertiary            = Tertiary40
val OnTertiary          = Neutral100
val TertiaryContainer   = Tertiary90
val OnTertiaryContainer = Tertiary10

val Background   = Neutral99
val OnBackground = Neutral10
val Surface      = Neutral95
val OnSurface    = Neutral10
val SurfaceVariant    = Neutral90
val OnSurfaceVariant  = Neutral20
val Outline           = Neutral80
val OutlineVariant    = Neutral90

val Error            = Error40
val OnError          = Neutral100
val ErrorContainer   = Error90
val OnErrorContainer = Error10

val GradientStart = Primary40
val GradientEnd   = Color(0xFF59131C)
val ButtonGradientStart = Color(0XFFE84E1C)
val ButtonGradientEnd = Color(0XFF5F151C)
val TopAppBarGradientStart = Color(0XFFE84E1C)
val TopAppBarGradientEnd = Color(0XFF91291C)

val CardAccent = Primary40

val StatusSuccess   = Color(0xFF0CBC2C)
val StatusPublished = StatusSuccess
val StatusSuspended = Color(0xFFDD2013)
val StatusPending   = Primary40
val StatusConfirmed = Tertiary40

val CardBackground  = Color(0xFFFEFAF8)
val SearchBarOverlay = Color(0xFF1C1B1B).copy(alpha = 0.21f)

val ServiceTileColors = listOf(
    Color(0xFFFFE4CC), // soft peach
    Color(0xFFD4EDFF), // sky blue
    Color(0xFFD8F5E4), // mint green
    Color(0xFFF5D6FF), // lavender
    Color(0xFFFFEBD4), // light apricot
    Color(0xFFD6F0FF), // pale cyan
    Color(0xFFFFD6E0), // rose pink
    Color(0xFFDFF5D6), // pale lime
)
