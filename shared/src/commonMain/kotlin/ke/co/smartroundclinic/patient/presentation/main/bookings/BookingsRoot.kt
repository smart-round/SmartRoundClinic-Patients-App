package ke.co.smartroundclinic.patient.presentation.main.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetMyAppointmentsUseCase
import ke.co.smartroundclinic.patient.presentation.main.Services.ServicesViewModel
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.AppointmentDetailsScreen
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.BookingAppointmentDetail
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.BookingsList
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.StatusConfirmed
import ke.co.smartroundclinic.patient.presentation.theme.StatusPending
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuspended
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookingsRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(BookingsList) }
    val isAtRoot = backStack.size == 1
    SideEffect { onAtRootChanged(isAtRoot) }

    val servicesVm: ServicesViewModel = koinViewModel()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<BookingsList> {
                BookingsListScreen(
                    onAppointmentClick = { appointmentId ->
                        backStack.add(BookingAppointmentDetail(appointmentId))
                    },
                )
            }
            entry<BookingAppointmentDetail> { dest ->
                AppointmentDetailsScreen(
                    appointmentId = dest.appointmentId,
                    appointment = servicesVm.appointmentDetail,
                    doctor = servicesVm.appointmentDetail?.let { servicesVm.doctorById(it.doctorId) },
                    onLoad = { id -> servicesVm.loadAppointmentDetail(id) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

@Composable
private fun BookingsListScreen(
    onAppointmentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val getMyAppointments: GetMyAppointmentsUseCase = koinInject()
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        isLoading = true
        when (val result = getMyAppointments()) {
            is Resource.Success -> appointments = result.data ?: emptyList()
            else -> {}
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            Text(
                text = "My Bookings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { scope.launch { load() } },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (isLoading && appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No bookings yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Book an appointment with a doctor\nto get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                ) {
                    items(appointments.sortedByDescending { it.bookedAt }, key = { it.id }) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onClick = { onAppointmentClick(appointment.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(appointment: Appointment, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Doctor row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Doctor avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
                    contentAlignment = Alignment.Center,
                ) {
                    if (appointment.doctorProfilePicture != null) {
                        AsyncImage(
                            model = appointment.doctorProfilePicture,
                            contentDescription = appointment.doctorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.doctorName?.let { formatDoctorName(it) } ?: "Doctor",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!appointment.doctorSpeciality.isNullOrBlank()) {
                        Text(
                            text = appointment.doctorSpeciality,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                StatusBadge(status = appointment.status)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Date & time
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(icon = Icons.Filled.CalendarToday, text = appointment.date)
                InfoChip(
                    icon = Icons.Filled.Schedule,
                    text = formatTimeRange(appointment.slotStart, appointment.slotEnd),
                )
            }

            if (!appointment.notes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = appointment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "confirmed" -> StatusConfirmed.copy(alpha = 0.15f) to StatusConfirmed
        "cancelled" -> StatusSuspended.copy(alpha = 0.15f) to StatusSuspended
        "completed" -> Color(0xFF4CAF50).copy(alpha = 0.15f) to Color(0xFF4CAF50)
        else -> StatusPending.copy(alpha = 0.15f) to StatusPending
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
        )
    }
}

private fun formatDoctorName(name: String): String {
    val stripped = name.removePrefix("Dr. ").removePrefix("Dr.").removePrefix("Dr ").trim()
    return "Dr. ${stripped.split(" ").take(2).joinToString(" ")}"
}

private fun formatTimeRange(start: String, end: String): String {
    fun fmt(t: String): String = try {
        val parts = t.split("T").lastOrNull()?.split(":") ?: return t
        val hour = parts[0].toIntOrNull() ?: return t
        val minute = parts.getOrNull(1) ?: "00"
        val ampm = if (hour < 12) "AM" else "PM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        "$h:$minute $ampm"
    } catch (e: Exception) { t }
    return "${fmt(start)} – ${fmt(end)}"
}
