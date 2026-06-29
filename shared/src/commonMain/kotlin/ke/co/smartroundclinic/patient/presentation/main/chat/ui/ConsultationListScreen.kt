package ke.co.smartroundclinic.patient.presentation.main.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.presentation.common.composables.PatientDashboardHeader
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import ke.co.smartroundclinic.patient.presentation.theme.StatusConfirmed
import ke.co.smartroundclinic.patient.presentation.theme.StatusPending
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuccess

@Composable
internal fun ConsultationListScreen(
    appointments: List<Appointment>,
    isLoading: Boolean,
    onAppointmentClick: (Appointment) -> Unit,
    onRefresh: () -> Unit,
    doctorName: (String) -> String,
    doctorPicture: (String) -> String?,
    canJoin: (Appointment) -> Boolean = { false },
    isOverdue: (Appointment) -> Boolean = { false },
    onCancelClick: (Appointment) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PatientDashboardHeader(
            title = "Consultations",
            onProfileClick = onProfileClick,
            onNotificationsClick = onNotificationsClick,
        )

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
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
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary90),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(imageVector = Icons.Filled.ChatBubbleOutline, contentDescription = null, tint = Primary40, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(text = "No Active Consultations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Confirmed appointments will appear here\nso you can join a consultation with your doctor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    items(appointments, key = { it.id }) { appointment ->
                        ConsultationCard(
                            appointment = appointment,
                            doctorName = doctorName(appointment.doctorId),
                            doctorPicture = doctorPicture(appointment.doctorId),
                            canJoin = canJoin(appointment),
                            isOverdue = isOverdue(appointment),
                            onCancelClick = { onCancelClick(appointment) },
                            onClick = { onAppointmentClick(appointment) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsultationCard(
    appointment: Appointment,
    doctorName: String,
    doctorPicture: String?,
    canJoin: Boolean,
    isOverdue: Boolean,
    onCancelClick: () -> Unit,
    onClick: () -> Unit,
) {
    val isConfirmed = appointment.status.equals("CONFIRMED", ignoreCase = true)
    val isCompleted = appointment.status.equals("COMPLETED", ignoreCase = true)
    val isClickable = !isConfirmed || canJoin

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable)
                    Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
                else
                    Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DoctorAvatar(picture = doctorPicture, size = 48)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dr. $doctorName",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = appointment.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when {
                    isOverdue -> OutlinedButton(
                        onClick = onCancelClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.height(28.dp),
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                    else -> StatusBadge(status = appointment.status)
                }
                when {
                    isCompleted || isOverdue -> Unit
                    canJoin -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Filled.VideoCall, contentDescription = null, tint = StatusConfirmed, modifier = Modifier.size(16.dp))
                        Text(text = "Join", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = StatusConfirmed)
                    }
                    isConfirmed -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Opens ${appointment.slotStart}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DoctorAvatar(picture: String?, size: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size.dp).clip(CircleShape).background(Primary90),
        contentAlignment = Alignment.Center,
    ) {
        if (!picture.isNullOrBlank()) {
            AsyncImage(
                model = picture,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Primary40,
                modifier = Modifier.size((size * 0.6).dp),
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "confirmed" -> StatusConfirmed.copy(alpha = 0.15f) to StatusConfirmed
        "completed" -> StatusSuccess.copy(alpha = 0.15f) to StatusSuccess
        else -> StatusPending.copy(alpha = 0.15f) to StatusPending
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
        )
    }
}
