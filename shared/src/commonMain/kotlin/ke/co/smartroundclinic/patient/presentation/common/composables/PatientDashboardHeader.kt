package ke.co.smartroundclinic.patient.presentation.common.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.notification
import ke.co.smartroundclinic.patient.presentation.main.notifications.NotificationsViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.ProfileViewModel
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

private val AvatarShape = RoundedCornerShape(8.dp)
private val HeaderShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

@Composable
fun PatientDashboardHeader(
    title: String = "",
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    bottomContent: (@Composable () -> Unit)? = null,
    profileViewModel: ProfileViewModel = koinViewModel(),
    notificationsViewModel: NotificationsViewModel = koinViewModel(),
) {
    val user by profileViewModel.user.collectAsState()
    val firstName = user?.fullName?.trim()?.split(" ")?.firstOrNull { it.isNotBlank() } ?: ""
    val profilePicture = user?.profilePicture
    val unreadCount = notificationsViewModel.unreadCount
    val isGreeting = title.isBlank()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)), HeaderShape)
            .clip(HeaderShape)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(AvatarShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onProfileClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    if (profilePicture != null) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(AvatarShape),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isGreeting) "Hello${if (firstName.isNotBlank()) " $firstName" else ""} 👋" else title,
                    // FontFamily.Default (not the brand Montserrat/Raleway fonts, which carry no
                    // emoji glyphs or fallback chain) — needed for the 👋 to render on iOS.
                    style = if (isGreeting) {
                        MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Default)
                    } else {
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    },
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onNotificationsClick,
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.notification),
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp),
                        )
                    }
                }
            }
            if (isGreeting) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your health, seamlessly\nmanaged in one place.",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            if (bottomContent != null) {
                Spacer(Modifier.height(8.dp))
                bottomContent()
            }
        }
    }
}
