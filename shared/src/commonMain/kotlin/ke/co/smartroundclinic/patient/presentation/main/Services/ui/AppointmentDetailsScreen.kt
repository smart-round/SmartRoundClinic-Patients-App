package ke.co.smartroundclinic.patient.presentation.main.Services.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import ke.co.smartroundclinic.patient.data.remote.dto.response.PaymentByAppointmentData
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.model.MedicalRecord
import ke.co.smartroundclinic.patient.domain.model.PrescriptionItem
import ke.co.smartroundclinic.patient.domain.model.Rating
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.common.composables.StarRatingDisplay
import ke.co.smartroundclinic.patient.presentation.common.composables.StarRatingInput
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import ke.co.smartroundclinic.patient.presentation.theme.Tertiary40
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.ShapeButton
import ke.co.smartroundclinic.patient.presentation.theme.ShapeCard
import ke.co.smartroundclinic.patient.presentation.theme.StatusConfirmed
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuccess
import ke.co.smartroundclinic.patient.presentation.theme.StatusPending
import ke.co.smartroundclinic.patient.presentation.theme.SnackbarError
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuspended
import ke.co.smartroundclinic.patient.common.todayLocalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(
    appointmentId: String,
    appointment: Appointment?,
    doctor: Doctor?,
    medicalRecord: MedicalRecord? = null,
    isLoadingMedicalRecord: Boolean = false,
    payment: PaymentByAppointmentData? = null,
    onLoad: (String) -> Unit,
    onBack: () -> Unit,
    onRebook: ((doctor: Doctor, previousAppointmentId: String) -> Unit)? = null,
    onCancel: ((id: String, reason: String?) -> Unit)? = null,
    isCancelling: Boolean = false,
    myRatingOfDoctor: Rating? = null,
    hasAlreadyRatedAppointment: Boolean = false,
    isSubmittingRating: Boolean = false,
    onSubmitRating: (Int, String?) -> Unit = { _, _ -> },
    onUpdateRating: (Int, String?) -> Unit = { _, _ -> },
    onDeleteRating: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(appointmentId) { onLoad(appointmentId) }

    var showCancelSheet by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var showCancelReasonError by remember { mutableStateOf(false) }
    val cancelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showCancelSheet) {
        ModalBottomSheet(
            onDismissRequest = { if (!isCancelling) showCancelSheet = false },
            sheetState = cancelSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Cancel Appointment",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "Are you sure you want to cancel this appointment? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = {
                        cancelReason = it
                        if (showCancelReasonError && it.isNotBlank()) showCancelReasonError = false
                    },
                    label = { Text("Reason") },
                    placeholder = { Text("Enter a reason…") },
                    isError = showCancelReasonError,
                    supportingText = {
                        if (showCancelReasonError) Text("A reason is required to cancel this appointment")
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(ShapeButton)
                        .background(SnackbarError)
                        .then(
                            if (!isCancelling)
                                Modifier.clickable {
                                    if (cancelReason.isBlank()) {
                                        showCancelReasonError = true
                                    } else {
                                        onCancel?.invoke(appointmentId, cancelReason.trim())
                                        showCancelSheet = false
                                    }
                                }
                            else Modifier,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Yes, Cancel",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                        )
                    }
                }
                TextButton(
                    onClick = { showCancelSheet = false },
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Keep Appointment",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(vertical = 4.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Text(
                text = "Appointment Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp),
            )
        }

        if (appointment == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Appointment #${appointment.id.takeLast(8).uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusPill(status = appointment.status)
            }

            // Doctor card
            if (doctor != null) {
                DetailCard(title = "Doctor") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Brush.verticalGradient(listOf(GradientStart, GradientEnd)),
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clip(RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (doctor.profilePicture != null) {
                                    AsyncImage(
                                        model = doctor.profilePicture,
                                        contentDescription = doctor.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = doctor.displayName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                )
                                if (doctor.specialization != null) {
                                    Text(
                                        text = doctor.specialization,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        if (payment != null) {
                            Text(
                                text = formatAmount(payment.amount, payment.currency),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }

            // Date & Time card
            DetailCard(title = "Date & Time") {
                DetailRow(
                    icon = Icons.Filled.CalendarToday,
                    label = "Date",
                    value = appointment.date,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                DetailRow(
                    icon = Icons.Filled.Schedule,
                    label = "Time",
                    value = formatTimeRange(appointment.slotStart, appointment.slotEnd),
                )
            }

            // Notes card
            if (!appointment.notes.isNullOrBlank()) {
                DetailCard(title = "Notes") {
                    Text(
                        text = appointment.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Cancellation info
            if (!appointment.cancellationReason.isNullOrBlank()) {
                DetailCard(title = "Cancellation Reason") {
                    Text(
                        text = appointment.cancellationReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Refund info — only present when a completed payment was cancelled
            appointment.refund?.let { refund ->
                DetailCard(title = "Refund") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatAmount(refund.amount, refund.currency),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        RefundStatusPill(status = refund.status)
                    }
                }
            }

            // Prescription & Notes
            PrescriptionSection(
                medicalRecord = medicalRecord,
                isLoading = isLoadingMedicalRecord,
            )

            // Rate Doctor — only for COMPLETED appointments
            if (appointment.status.lowercase() == "completed") {
                RateDoctorCard(
                    myRating = myRatingOfDoctor,
                    hasAlreadyRated = hasAlreadyRatedAppointment,
                    isSubmitting = isSubmittingRating,
                    onSubmit = onSubmitRating,
                    onUpdate = onUpdateRating,
                    onDelete = onDeleteRating,
                )
            }

            // Rebook button — only for COMPLETED appointments within the same calendar month
            val canRebook = appointment.status.lowercase() == "completed" &&
                onRebook != null &&
                isWithin30Days(appointment.date)
            if (canRebook) {
                Spacer(Modifier.height(8.dp))
                // Use the loaded doctor if available, otherwise build a minimal stub from appointment fields
                val rebookDoctor = doctor ?: Doctor(
                    id = appointment.doctorId,
                    profileId = "",
                    name = appointment.doctorName ?: "Doctor",
                    profilePicture = appointment.doctorProfilePicture,
                    specialization = appointment.doctorSpeciality,
                    specializationId = null,
                    facilityName = null,
                    averageRating = 0.0,
                    totalReviews = 0,
                    totalBookings = 0,
                    yearsOfExperience = null,
                )
                Button(
                    onClick = { onRebook(rebookDoctor, appointment.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Rebook Follow-Up",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            // Cancel button — hide only for terminal statuses
            val canCancel = onCancel != null &&
                appointment.status.lowercase() !in setOf("no_show", "completed", "cancelled")
            if (canCancel) {
                Button(
                    onClick = {
                        cancelReason = ""
                        showCancelReasonError = false
                        showCancelSheet = true
                    },
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SnackbarError),
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Cancel Appointment",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PrescriptionSection(
    medicalRecord: MedicalRecord?,
    isLoading: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Prescription & Notes",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary40, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
                medicalRecord == null -> {
                    Text(
                        text = "No prescription added yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    if (!medicalRecord.diagnosis.isNullOrBlank()) {
                        Text(
                            text = "Diagnosis",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = medicalRecord.diagnosis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    if (medicalRecord.prescription.isNotEmpty()) {
                        Text(
                            text = "Prescription",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Primary40,
                        )
                        Spacer(Modifier.height(6.dp))
                        medicalRecord.prescription.forEach { item ->
                            PrescriptionItemRow(item)
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    if (!medicalRecord.summary.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = medicalRecord.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    if (!medicalRecord.referralNote.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Referral",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = medicalRecord.referralNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    if (!medicalRecord.additionalNotes.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Additional Notes",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = medicalRecord.additionalNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    if (medicalRecord.labRequests.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Lab Requests",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        medicalRecord.labRequests.forEach { lab ->
                            Text(
                                text = "• $lab",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrescriptionItemRow(item: PrescriptionItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary90, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = item.drug,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${item.dosage} • ${item.frequency} • ${item.duration}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!item.instructions.isNullOrBlank()) {
            Text(
                text = item.instructions,
                style = MaterialTheme.typography.labelSmall,
                color = Tertiary40,
            )
        }
    }
}

@Composable
private fun RateDoctorCard(
    myRating: Rating?,
    hasAlreadyRated: Boolean,
    isSubmitting: Boolean,
    onSubmit: (Int, String?) -> Unit,
    onUpdate: (Int, String?) -> Unit,
    onDelete: () -> Unit,
) {
    var isEditing by remember(myRating) { mutableStateOf(myRating == null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Rating") },
            text = { Text("Are you sure you want to delete this rating?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    DetailCard(title = "Rate This Doctor") {
        when {
            myRating != null && !isEditing -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StarRatingDisplay(rating = myRating.rating.toDouble(), starSize = 20.dp)
                    if (!myRating.comment.isNullOrBlank()) {
                        Text(
                            text = myRating.comment,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row {
                        TextButton(onClick = { isEditing = true }) {
                            Text("Edit", style = MaterialTheme.typography.labelSmall, color = Tertiary40)
                        }
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text("Delete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            hasAlreadyRated -> {
                Text(
                    text = "You've already rated this appointment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                RatingForm(
                    initialRating = myRating?.rating ?: 0,
                    initialComment = myRating?.comment.orEmpty(),
                    isSubmitting = isSubmitting,
                    submitLabel = if (myRating != null) "Save Changes" else "Submit Rating",
                    onCancel = if (myRating != null) ({ isEditing = false }) else null,
                    onSubmit = { rating, comment ->
                        if (myRating != null) onUpdate(rating, comment) else onSubmit(rating, comment)
                    },
                )
            }
        }
    }
}

@Composable
private fun RatingForm(
    initialRating: Int,
    initialComment: String,
    isSubmitting: Boolean,
    submitLabel: String,
    onCancel: (() -> Unit)?,
    onSubmit: (Int, String?) -> Unit,
) {
    var rating by remember { mutableStateOf(initialRating) }
    var comment by remember { mutableStateOf(initialComment) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StarRatingInput(rating = rating, onRatingChange = { rating = it })
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = { Text("Add a comment (optional)", style = MaterialTheme.typography.bodySmall) },
            shape = ShapeCard,
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onCancel != null) {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
            PrimaryButton(
                onClick = { onSubmit(rating, comment.ifBlank { null }) },
                enabled = rating > 0 && !isSubmitting,
                modifier = Modifier.weight(1f),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(
                        text = submitLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val (bg, text, label) = when (status.lowercase()) {
        "confirmed" -> Triple(StatusConfirmed.copy(alpha = 0.15f), StatusConfirmed, "Confirmed")
        "cancelled" -> Triple(StatusSuspended.copy(alpha = 0.15f), StatusSuspended, "Cancelled")
        "completed" -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, "Completed")
        else -> Triple(StatusPending.copy(alpha = 0.15f), StatusPending, status.replaceFirstChar { it.uppercase() })
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = text,
        )
    }
}

@Composable
private fun RefundStatusPill(status: String) {
    val (text, label) = when (status.lowercase()) {
        "completed" -> StatusSuccess to "Refunded"
        "failed" -> StatusSuspended to "Failed"
        else -> StatusPending to "Pending"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = text,
    )
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatAmount(amount: Double, currency: String): String {
    val whole = amount.toLong()
    val cents = ((amount - whole) * 100).toLong()
    return "$currency $whole.${cents.toString().padStart(2, '0')}"
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

// Rebooking is allowed if the appointment was within the last 30 days
private fun isWithin30Days(appointmentDate: String): Boolean = try {
    val today = todayLocalDate()
    val parts = appointmentDate.substringBefore("T").split("-")
    val apptDate = LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    val daysAgo = apptDate.until(today, DateTimeUnit.DAY)
    daysAgo in 0..30
} catch (e: Exception) { false }

private val Doctor.displayName: String
    get() {
        val stripped = name.removePrefix("Dr. ").removePrefix("Dr.").removePrefix("Dr ").trim()
        return "Dr. ${stripped.split(" ").take(2).joinToString(" ")}"
    }
