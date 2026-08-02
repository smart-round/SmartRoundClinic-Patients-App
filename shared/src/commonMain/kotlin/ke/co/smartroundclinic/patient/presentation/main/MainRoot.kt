package ke.co.smartroundclinic.patient.presentation.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.bottom_bar_cost_estimate
import ke.co.smartroundclinic.patient.generated.resources.bottom_bar_group
import ke.co.smartroundclinic.patient.generated.resources.bottom_bar_home
import ke.co.smartroundclinic.patient.generated.resources.bottom_bar_layers
import ke.co.smartroundclinic.patient.generated.resources.bottom_bar_vector
import ke.co.smartroundclinic.patient.core.notification.NotificationDeepLink
import ke.co.smartroundclinic.patient.core.notification.NotificationEvent
import ke.co.smartroundclinic.patient.presentation.main.articles.ArticlesRoot
import ke.co.smartroundclinic.patient.presentation.main.bookings.BookingsRoot
import ke.co.smartroundclinic.patient.presentation.main.chat.ChatRoot
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationCall
import ke.co.smartroundclinic.patient.presentation.main.chat.destinations.ConsultationChat
import ke.co.smartroundclinic.patient.presentation.main.destinations.Appointments
import ke.co.smartroundclinic.patient.presentation.main.destinations.Articles
import ke.co.smartroundclinic.patient.presentation.main.destinations.Bookings
import ke.co.smartroundclinic.patient.presentation.main.destinations.Doctors
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.presentation.main.destinations.Home
import ke.co.smartroundclinic.patient.presentation.main.Services.ServicesRoot
import ke.co.smartroundclinic.patient.presentation.main.home.HomeRoot
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.Notifications
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.UserProfile
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class BottomTab(
    val destination: NavKey,
    val label: String,
    val icon: DrawableResource,
)

private val tabs = listOf(
    BottomTab(Home, "Home", Res.drawable.bottom_bar_home),
    BottomTab(Doctors, "Services", Res.drawable.bottom_bar_layers),
    BottomTab(Bookings, "Bookings", Res.drawable.bottom_bar_group),
    BottomTab(Articles, "Articles", Res.drawable.bottom_bar_cost_estimate),
    BottomTab(Appointments, "Chat", Res.drawable.bottom_bar_vector),
)

@Composable
fun MainRoot(modifier: Modifier = Modifier, onSignOut: () -> Unit = {}) {
    val backStack = retain { mutableStateListOf<NavKey>(Home) }
    val currentTab = backStack.lastOrNull() ?: Home

    var isAtRoot by remember { mutableStateOf(true) }
    var pendingHomeDestinations by remember { mutableStateOf<List<NavKey>>(emptyList()) }
    var pendingBookingAppointmentId by remember { mutableStateOf<String?>(null) }
    var pendingConsultation by remember { mutableStateOf<ConsultationChat?>(null) }
    var pendingCall by remember { mutableStateOf<ConsultationCall?>(null) }
    var pendingSupportTicketId by remember { mutableStateOf<String?>(null) }
    var pendingMedicalHistory by remember { mutableStateOf(false) }
    var pendingDoctorProfile by remember { mutableStateOf<Doctor?>(null) }
    var pendingReferralId by remember { mutableStateOf<String?>(null) }

    fun selectTab(dest: NavKey) {
        if (currentTab != dest) {
            backStack.clear()
            backStack.add(dest)
            isAtRoot = true
        }
    }

    LaunchedEffect(Unit) {
        NotificationDeepLink.pendingEvent.collect { event ->
            if (event == null) return@collect
            NotificationDeepLink.consume()
            when (event) {
                is NotificationEvent.ToNotifications -> {
                    selectTab(Home)
                    pendingHomeDestinations = listOf(Notifications)
                }
                is NotificationEvent.ToAppointmentDetail -> {
                    selectTab(Bookings)
                    pendingBookingAppointmentId = event.appointmentId
                }
                is NotificationEvent.ToConsultationChat -> {
                    selectTab(Appointments)
                    pendingConsultation = ConsultationChat(event.doctorId, event.doctorName, event.appointmentId)
                }
                is NotificationEvent.ToCall -> {
                    selectTab(Appointments)
                    pendingConsultation = ConsultationChat(event.doctorId, event.doctorName, event.appointmentId)
                    pendingCall = ConsultationCall(event.doctorId, isVideo = true)
                }
                is NotificationEvent.ToSupportTicket -> {
                    selectTab(Home)
                    pendingSupportTicketId = event.ticketId
                }
                is NotificationEvent.ToMedicalHistory -> {
                    selectTab(Home)
                    pendingMedicalHistory = true
                }
                is NotificationEvent.ToReferrals -> {
                    selectTab(Bookings)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { if (isAtRoot) BottomNavBar(currentTab = currentTab, onTabSelected = ::selectTab) },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            backStack = backStack,
            onBack = {
                if (currentTab != Home) {
                    backStack.clear()
                    backStack.add(Home)
                    isAtRoot = true
                }
            },
            entryProvider = entryProvider {
                entry<Home> {
                    HomeRoot(
                        onAtRootChanged = { isAtRoot = it },
                        onSignOut = onSignOut,
                        onNavigateToServices = { selectTab(Doctors) },
                        pendingDestinations = pendingHomeDestinations,
                        onPendingNavigated = { pendingHomeDestinations = emptyList() },
                        pendingSupportTicketId = pendingSupportTicketId,
                        onPendingSupportTicketNavigated = { pendingSupportTicketId = null },
                        pendingMedicalHistory = pendingMedicalHistory,
                        onPendingMedicalHistoryNavigated = { pendingMedicalHistory = false },
                    )
                }
                entry<Appointments> {
                    ChatRoot(
                        onAtRootChanged = { isAtRoot = it },
                        pendingConversation = pendingConsultation,
                        onPendingNavigated = { pendingConsultation = null },
                        pendingCall = pendingCall,
                        onPendingCallNavigated = { pendingCall = null },
                        onProfileClick = { selectTab(Home); pendingHomeDestinations = listOf(UserProfile) },
                        onNotificationsClick = { selectTab(Home); pendingHomeDestinations = listOf(Notifications) },
                    )
                }
                entry<Doctors> {
                    ServicesRoot(
                        onAtRootChanged = { isAtRoot = it },
                        onProfileClick = { selectTab(Home); pendingHomeDestinations = listOf(UserProfile) },
                        onNotificationsClick = { selectTab(Home); pendingHomeDestinations = listOf(Notifications) },
                        pendingDoctorProfile = pendingDoctorProfile,
                        onPendingDoctorProfileNavigated = { pendingDoctorProfile = null },
                        pendingReferralId = pendingReferralId,
                    )
                }
                entry<Articles> {
                    ArticlesRoot(
                        onAtRootChanged = { isAtRoot = it },
                        onProfileClick = { selectTab(Home); pendingHomeDestinations = listOf(UserProfile) },
                        onNotificationsClick = { selectTab(Home); pendingHomeDestinations = listOf(Notifications) },
                    )
                }
                entry<Bookings> {
                    BookingsRoot(
                        onAtRootChanged = { isAtRoot = it },
                        pendingAppointmentId = pendingBookingAppointmentId,
                        onPendingNavigated = { pendingBookingAppointmentId = null },
                        onProfileClick = { selectTab(Home); pendingHomeDestinations = listOf(UserProfile) },
                        onNotificationsClick = { selectTab(Home); pendingHomeDestinations = listOf(Notifications) },
                        onBookReferredDoctor = { doctor, referralId ->
                            pendingDoctorProfile = doctor
                            pendingReferralId = referralId
                            selectTab(Doctors)
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun BottomNavBar(
    currentTab: NavKey,
    onTabSelected: (NavKey) -> Unit,
) {
    Column {
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding(),
        ) {
            tabs.forEach { tab ->
                val isSelected = currentTab::class == tab.destination::class
                BottomNavItem(
                    tab = tab,
                    isSelected = isSelected,
                    onSelected = { onTabSelected(tab.destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomTab,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "tabIconColor",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "tabLabelColor",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onSelected,
            )
            .padding(bottom = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 3.dp)
                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ),
        )
        Spacer(Modifier.height(6.dp))
        Icon(
            painter = painterResource(tab.icon),
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
        )
    }
}
