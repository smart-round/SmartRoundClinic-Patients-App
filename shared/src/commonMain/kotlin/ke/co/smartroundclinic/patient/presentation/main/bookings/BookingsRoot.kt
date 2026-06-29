package ke.co.smartroundclinic.patient.presentation.main.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryItem
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetMyAppointmentsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetPaymentHistoryUseCase
import ke.co.smartroundclinic.patient.presentation.main.Services.ServicesViewModel
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.AppointmentDetailsScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.BookAppointmentScreen
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.BookingAppointmentDetail
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.BookingsList
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.PaymentHistoryDetail
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.RebookFromBookings
import ke.co.smartroundclinic.patient.presentation.main.bookings.ui.PaymentHistoryDetailScreen
import ke.co.smartroundclinic.patient.presentation.main.bookings.ui.PaymentStatusChip
import ke.co.smartroundclinic.patient.presentation.common.composables.PatientDashboardHeader
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.ShapeCard
import ke.co.smartroundclinic.patient.presentation.theme.ShapePill
import ke.co.smartroundclinic.patient.presentation.theme.StatusConfirmed
import ke.co.smartroundclinic.patient.presentation.theme.StatusPending
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuccess
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuspended
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.card_payment
import ke.co.smartroundclinic.patient.generated.resources.mpesa
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookingsRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    pendingAppointmentId: String? = null,
    onPendingNavigated: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(BookingsList) }
    val isAtRoot = backStack.size == 1
    SideEffect { onAtRootChanged(isAtRoot) }

    LaunchedEffect(pendingAppointmentId) {
        if (!pendingAppointmentId.isNullOrBlank()) {
            backStack.removeAll { it is BookingAppointmentDetail }
            backStack.add(BookingAppointmentDetail(pendingAppointmentId))
            onPendingNavigated()
        }
    }

    val servicesVm: ServicesViewModel = koinViewModel()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<BookingsList> {
                BookingsListScreen(
                    onAppointmentClick = { backStack.add(BookingAppointmentDetail(it)) },
                    onPaymentClick = { backStack.add(PaymentHistoryDetail(it)) },
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                )
            }
            entry<BookingAppointmentDetail> { dest ->
                AppointmentDetailsScreen(
                    appointmentId = dest.appointmentId,
                    appointment = servicesVm.appointmentDetail,
                    doctor = servicesVm.appointmentDetail?.let { servicesVm.doctorById(it.doctorId) },
                    medicalRecord = servicesVm.medicalRecord,
                    isLoadingMedicalRecord = servicesVm.isLoadingMedicalRecord,
                    onLoad = { id -> servicesVm.loadAppointmentDetail(id) },
                    onBack = { backStack.removeLastOrNull() },
                    onRebook = { doctor, previousAppointmentId ->
                        servicesVm.cacheDoctor(doctor)
                        backStack.add(RebookFromBookings(doctor.id, previousAppointmentId))
                    },
                    onCancel = { id, reason -> servicesVm.cancelAppointment(id, reason) },
                    isCancelling = servicesVm.isCancelling,
                )
            }
            entry<RebookFromBookings> { dest ->
                BookAppointmentScreen(
                    doctor = servicesVm.doctorById(dest.doctorId) ?: return@entry,
                    calendarView = servicesVm.calendarView,
                    availableSlots = servicesVm.availableSlots,
                    isLoadingSlots = servicesVm.isLoadingSlots,
                    isStkInitiating = servicesVm.isStkInitiating,
                    isBooking = servicesVm.isBooking,
                    stkPushData = servicesVm.stkPushData,
                    stkPollState = servicesVm.stkPollState,
                    stkError = servicesVm.stkError,
                    bookedAppointmentId = servicesVm.bookedAppointment?.id,
                    bookingError = servicesVm.bookingError,
                    isRebooking = true,
                    previousAppointmentId = dest.previousAppointmentId,
                    onLoadCalendar = { yearMonth -> servicesVm.loadCalendarView(dest.doctorId, yearMonth) },
                    onLoadSlots = { date -> servicesVm.loadSlots(dest.doctorId, date) },
                    onInitiateStkPush = { date, slotStart, phoneNumber ->
                        servicesVm.initiateStkPush(
                            doctorId = dest.doctorId,
                            date = date,
                            slotStart = slotStart,
                            phoneNumber = phoneNumber,
                            isRebooking = true,
                            previousAppointmentId = dest.previousAppointmentId,
                        )
                    },
                    onDismissStkPush = { servicesVm.dismissStkPush() },
                    onViewBooking = { appointmentId ->
                        servicesVm.clearBookingState()
                        backStack.removeLastOrNull()
                        backStack.add(BookingAppointmentDetail(appointmentId))
                    },
                    onDismissResult = { servicesVm.clearBookingState() },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<PaymentHistoryDetail> { dest ->
                PaymentHistoryDetailScreen(
                    paymentId = dest.paymentId,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

@Composable
private fun BookingsListScreen(
    onAppointmentClick: (String) -> Unit,
    onPaymentClick: (String) -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Appointments", "Payments")

    Column(modifier = modifier.fillMaxSize()) {
        PatientDashboardHeader(
            title = "My Bookings",
            onProfileClick = onProfileClick,
            onNotificationsClick = onNotificationsClick,
        )

        // Segmented tab row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .shadow(elevation = 4.dp, shape = ShapePill)
                .clip(ShapePill)
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
        ) {
            Row {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(ShapePill)
                            .then(if (isSelected) Modifier.background(Color.White) else Modifier)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { selectedTab = index },
                            )
                            .padding(vertical = 10.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> AppointmentsTab(onAppointmentClick = onAppointmentClick)
            1 -> PaymentsTab(onPaymentClick = onPaymentClick)
        }
    }
}

// ─── Appointments tab ────────────────────────────────────────────────────────

@Composable
private fun AppointmentsTab(onAppointmentClick: (String) -> Unit) {
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

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { scope.launch { load() } },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isLoading && appointments.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (appointments.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.EventBusy, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                title = "No bookings yet",
                subtitle = "Book an appointment with a doctor\nto get started.",
            )
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

// ─── Payments tab ─────────────────────────────────────────────────────────────

@Composable
private fun PaymentsTab(onPaymentClick: (String) -> Unit) {
    val getHistory: GetPaymentHistoryUseCase = koinInject()
    var payments by remember { mutableStateOf<List<GetPaymentHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(false) }
    var nextPageToFetch by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val nearBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - 3
        }
    }

    LaunchedEffect(nearBottom) {
        if (nearBottom && hasMore && !isLoadingMore) {
            isLoadingMore = true
            when (val result = getHistory(page = nextPageToFetch)) {
                is Resource.Success -> {
                    val items = result.data?.data?.items ?: emptyList()
                    payments = (payments + items).sortedByDescending { it.createdAt }
                    nextPageToFetch--
                    hasMore = nextPageToFetch > 1
                }
                else -> {}
            }
            isLoadingMore = false
        }
    }

    suspend fun loadInitial() {
        isLoading = true
        payments = emptyList()
        hasMore = false
        nextPageToFetch = 0

        when (val discovery = getHistory(page = 1)) {
            is Resource.Success -> {
                val data = discovery.data?.data ?: run { isLoading = false; return }
                val total = data.pages.coerceAtLeast(1)
                val page1Items = data.items

                if (total == 1) {
                    payments = page1Items.sortedByDescending { it.createdAt }
                } else {
                    // Load newest page before revealing content — avoids flash of oldest payments
                    when (val newest = getHistory(page = total)) {
                        is Resource.Success -> {
                            val newestItems = newest.data?.data?.items ?: emptyList()
                            payments = (page1Items + newestItems).sortedByDescending { it.createdAt }
                            nextPageToFetch = total - 1
                            hasMore = nextPageToFetch > 1
                        }
                        else -> payments = page1Items.sortedByDescending { it.createdAt }
                    }
                }
            }
            else -> {}
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { loadInitial() }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { scope.launch { loadInitial() } },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isLoading && payments.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (payments.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.Receipt, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                title = "No payments yet",
                subtitle = "Your payment history will\nappear here after your first booking.",
            )
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            ) {
                items(payments, key = { it.id }) { payment ->
                    PaymentCard(
                        payment = payment,
                        onClick = { onPaymentClick(payment.id) },
                    )
                }
                when {
                    isLoadingMore -> item(key = "loading_more") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                    !hasMore -> item(key = "end_of_list") {
                        Text(
                            text = "· All payments loaded ·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentCard(payment: GetPaymentHistoryItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Payment method icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    val painter = if (payment.paymentMethod.contains("MPESA", ignoreCase = true) ||
                        payment.paymentMethod.contains("M-PESA", ignoreCase = true))
                        painterResource(Res.drawable.mpesa)
                    else
                        painterResource(Res.drawable.card_payment)
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = payment.paymentMethod,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = payment.paymentMethod,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = formatPaymentDate(payment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${payment.currency} ${payment.amount.toAmountString()}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    PaymentStatusChip(status = payment.status)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val ref = payment.mpesaReference?.takeIf { it.isNotBlank() }
                    ?: payment.invoiceId?.takeIf { it.isNotBlank() }
                    ?: "—"
                InfoChip(label = "Ref", value = ref)
                payment.netAmount?.let { InfoChip(label = "Net", value = "${payment.currency} $it") }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
) {
    Box(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Shared appointment card (unchanged logic) ───────────────────────────────

@Composable
private fun AppointmentCard(appointment: Appointment, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 6.dp),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Primary40),
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Appointment Date/Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    StatusBadge(status = appointment.status)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Primary40,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${appointment.date}  ·  ${formatTimeRange(appointment.slotStart, appointment.slotEnd)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Primary40,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                            Icon(Icons.Filled.Person, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = appointment.doctorName?.let { formatDoctorName(it) } ?: "Doctor",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = onClick,
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary40),
                    ) {
                        Text(text = "View", style = MaterialTheme.typography.labelMedium, color = Primary40, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "confirmed" -> StatusConfirmed.copy(alpha = 0.15f) to StatusConfirmed
        "cancelled" -> StatusSuspended.copy(alpha = 0.15f) to StatusSuspended
        "completed" -> StatusSuccess.copy(alpha = 0.15f) to StatusSuccess
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

private fun Double.toAmountString(): String {
    val whole = toLong()
    val cents = ((this - whole) * 100).toLong()
    return "$whole.${cents.toString().padStart(2, '0')}"
}

private fun formatPaymentDate(iso: String): String = try {
    val date = iso.substringBefore("T")
    val time = iso.substringAfter("T").substringBefore(".").substringBefore("Z")
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val min = parts.getOrElse(1) { "00" }
    val ampm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    "$date · $h:$min $ampm"
} catch (e: Exception) { iso }
