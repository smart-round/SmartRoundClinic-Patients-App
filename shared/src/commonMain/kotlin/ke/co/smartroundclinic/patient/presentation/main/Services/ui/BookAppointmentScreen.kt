package ke.co.smartroundclinic.patient.presentation.main.Services.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ke.co.smartroundclinic.patient.domain.model.CalendarDay
import ke.co.smartroundclinic.patient.domain.model.CalendarView
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.presentation.common.composables.PrimaryButton
import ke.co.smartroundclinic.patient.presentation.main.Services.PendingBookingPayment
import ke.co.smartroundclinic.patient.presentation.main.Services.StkPushResult
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.ShapeButton
import ke.co.smartroundclinic.patient.presentation.theme.ShapeCard
import ke.co.smartroundclinic.patient.common.todayLocalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
private val DAY_LABELS = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    doctor: Doctor,
    calendarView: CalendarView?,
    availableSlots: List<String>,
    isLoadingSlots: Boolean,
    isStkInitiating: Boolean,
    isBooking: Boolean,
    stkPushData: StkPushResult?,
    stkPollState: String?,
    stkError: String?,
    bookedAppointmentId: String?,
    bookingError: String?,
    pendingBookingPayment: PendingBookingPayment? = null,
    onLoadCalendar: (yearMonth: String) -> Unit,
    onLoadSlots: (date: String) -> Unit,
    isRebooking: Boolean = false,
    previousAppointmentId: String? = null,
    onInitiateStkPush: (date: String, slotStart: String, phoneNumber: String) -> Unit,
    onRetryBooking: (date: String, slotStart: String) -> Unit = { _, _ -> },
    onDismissStkPush: () -> Unit,
    onViewBooking: (appointmentId: String) -> Unit,
    onDismissResult: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stkSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden },
    )
    val today = todayLocalDate()
    var displayYear by remember { mutableStateOf(today.year) }
    var displayMonth by remember { mutableStateOf(today.month.number) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var showStkSheet by remember { mutableStateOf(false) }

    // An already-paid transactionRef for this doctor is waiting on a successful booking —
    // the CTA below reuses it instead of starting a new STK push.
    val hasPendingPayment = pendingBookingPayment != null && pendingBookingPayment.doctorId == doctor.id

    val yearMonthStr = "$displayYear-${displayMonth.toString().padStart(2, '0')}"

    LaunchedEffect(yearMonthStr) {
        onLoadCalendar(yearMonthStr)
    }

    LaunchedEffect(selectedDate) {
        selectedDate?.let { onLoadSlots(it) }
        selectedSlot = null
    }

    // Jump straight to the date that was already paid for, so the refreshed slot list
    // (fetched by the ViewModel the moment the conflict/resume happened) is visible immediately.
    LaunchedEffect(pendingBookingPayment) {
        val pending = pendingBookingPayment
        if (pending != null && pending.doctorId == doctor.id && selectedDate == null) {
            selectedDate = pending.date
        }
    }

    // A booking attempt that ends without success and without a terminal (409) error, while a
    // payment is still pending, was a retryable 400 slot conflict — close the sheet and drop
    // back to the picker instead of leaving the patient stuck on a payment-flow dead end.
    LaunchedEffect(isBooking, bookedAppointmentId, bookingError, hasPendingPayment) {
        if (!isBooking && bookedAppointmentId == null && bookingError == null && hasPendingPayment) {
            showStkSheet = false
        }
    }

    // Keep showStkSheet true so the success state is visible inside the sheet

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    text = if (isRebooking) "Pick a New Date/Time" else "Pick a Date/Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 88.dp),
            ) {
                Spacer(Modifier.height(16.dp))

                // Month navigator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            if (displayMonth == 1) {
                                displayMonth = 12; displayYear--
                            } else {
                                displayMonth--
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                        )
                    }
                    Text(
                        text = "${MONTHS[displayMonth - 1]} $displayYear",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    IconButton(
                        onClick = {
                            if (displayMonth == 12) {
                                displayMonth = 1; displayYear++
                            } else {
                                displayMonth++
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next month",
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Calendar grid
                CalendarGrid(
                    year = displayYear,
                    month = displayMonth,
                    calendarView = calendarView,
                    today = today,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(24.dp))

                // Time slots
                Text(
                    text = "Available Time Slots",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(12.dp))

                if (selectedDate == null) {
                    Text(
                        text = "Select a date to see available slots",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                } else if (isLoadingSlots) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (availableSlots.isEmpty()) {
                    Text(
                        text = "No slots available for this date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        availableSlots.forEach { slot ->
                            SlotChip(
                                slot = slot,
                                isSelected = selectedSlot == slot,
                                onClick = { selectedSlot = if (selectedSlot == slot) null else slot },
                            )
                        }
                    }
                }
            }
        }

        // Pay Now / Confirm Booking button — reuses an already-paid transactionRef instead of
        // starting a new payment whenever one is pending (e.g. after a slot-conflict retry).
        Button(
            onClick = {
                val date = selectedDate
                val slot = selectedSlot
                if (date != null && slot != null) {
                    if (hasPendingPayment) onRetryBooking(date, slot) else showStkSheet = true
                }
            },
            enabled = selectedDate != null && selectedSlot != null && !isStkInitiating && !isBooking,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            if (isBooking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = when {
                        hasPendingPayment -> "Confirm Booking — Already Paid"
                        isRebooking -> "Pay Follow-Up Fee"
                        else -> "Pay Now"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }

        AnimatedVisibility(
            visible = bookingError != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (bookingError != null) {
                PaymentFailedDialog(
                    message = bookingError,
                    onTryAgain = onDismissResult,
                )
            }
        }
    }

    // STK push bottom sheet
    if (showStkSheet || bookedAppointmentId != null || stkPushData != null ||
        stkPollState == "FAILED" || (!isStkInitiating && stkPushData == null && stkError != null)
    ) {
        StkPushSheet(
            sheetState = stkSheetState,
            isStkInitiating = isStkInitiating,
            isBooking = isBooking,
            stkPushData = stkPushData,
            stkPollState = stkPollState,
            stkError = stkError,
            bookedAppointmentId = bookedAppointmentId,
            onSendStkPush = { phoneNumber ->
                val date = selectedDate ?: return@StkPushSheet
                val slot = selectedSlot ?: return@StkPushSheet
                onInitiateStkPush(date, slot, phoneNumber)
            },
            onViewAppointment = {
                if (bookedAppointmentId != null) onViewBooking(bookedAppointmentId)
            },
            onDismiss = {
                showStkSheet = false
                onDismissResult()
                onDismissStkPush()
            },
        )
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    calendarView: CalendarView?,
    today: LocalDate,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDay = LocalDate(year, month, 1)
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }
    val firstDayOfWeek = (firstDay.dayOfWeek.ordinal + 1) % 7

    val availableDates: Set<String>? = calendarView?.days
        ?.filter { day ->
            day.isWorkingDay && day.slots.any { it.status.equals("AVAILABLE", ignoreCase = true) }
        }
        ?.map { it.date }
        ?.toSet()

    val cells = firstDayOfWeek + daysInMonth
    val rows = (cells + 6) / 7

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_LABELS.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Box {
            Column {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val day = cellIndex - firstDayOfWeek + 1
                            if (day < 1 || day > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val dateStr = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                                val localDate = LocalDate(year, month, day)
                                val isPast = localDate < today
                                val isEnabled = !isPast && availableDates?.contains(dateStr) == true
                                val isSelected = selectedDate == dateStr
                                val isToday = localDate == today

                                DayCell(
                                    day = day,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    isEnabled = isEnabled,
                                    onClick = { if (isEnabled) onDateSelected(dateStr) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    if (row < rows - 1) Spacer(Modifier.height(4.dp))
                }
            }

            if (calendarView == null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .then(
                    when {
                        isSelected -> Modifier.background(MaterialTheme.colorScheme.primary)
                        isToday -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else -> Modifier
                    },
                )
                .clickable(
                    enabled = isEnabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                ),
                color = when {
                    isSelected -> Color.White
                    !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun SlotChip(
    slot: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val formattedSlot = formatSlotTime(slot)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline,
        ) else null,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = formattedSlot,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

private fun formatSlotTime(slot: String): String {
    return try {
        val parts = slot.split("T").lastOrNull()?.split(":")
            ?: return slot
        val hour = parts[0].toIntOrNull() ?: return slot
        val minute = parts.getOrNull(1) ?: "00"
        val ampm = if (hour < 12) "AM" else "PM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        "$h:$minute $ampm"
    } catch (e: Exception) {
        slot
    }
}

@Composable
private fun PaymentFailedDialog(
    message: String,
    onTryAgain: () -> Unit,
) {
    Dialog(
        onDismissRequest = onTryAgain,
        properties = DialogProperties(dismissOnClickOutside = true),
    ) {
        Card(
            shape = ShapeCard,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Payment Failed",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message.ifBlank { "Something went wrong. Please try again." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    onClick = onTryAgain,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ShapeButton,
                ) {
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}
