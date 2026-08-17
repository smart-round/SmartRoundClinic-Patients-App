package ke.co.smartroundclinic.patient.presentation.main.bookings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.model.Referral
import ke.co.smartroundclinic.patient.domain.model.ReferralStatus
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetMyAppointmentsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorByIdUseCase
import ke.co.smartroundclinic.patient.domain.usecase.referral.AcceptReferralUseCase
import ke.co.smartroundclinic.patient.domain.usecase.referral.DeclineReferralUseCase
import ke.co.smartroundclinic.patient.domain.usecase.referral.GetReferralHistoryUseCase
import ke.co.smartroundclinic.patient.presentation.main.Services.ServicesViewModel
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.AppointmentDetailsScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.BookAppointmentScreen
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.BookingAppointmentDetail
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.BookingsList
import ke.co.smartroundclinic.patient.presentation.main.bookings.destinations.RebookFromBookings
import ke.co.smartroundclinic.patient.presentation.common.composables.PatientDashboardHeader
import ke.co.smartroundclinic.patient.presentation.theme.ButtonGradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.ButtonGradientStart
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Neutral20
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.ReferralAcceptedAccent
import ke.co.smartroundclinic.patient.presentation.theme.ReferralAcceptedSurface
import ke.co.smartroundclinic.patient.presentation.theme.ReferralDeclineAction
import ke.co.smartroundclinic.patient.presentation.theme.ReferralDeclinedAccent
import ke.co.smartroundclinic.patient.presentation.theme.ReferralDeclinedSurface
import ke.co.smartroundclinic.patient.presentation.theme.ShapeCard
import ke.co.smartroundclinic.patient.presentation.theme.ShapePill
import ke.co.smartroundclinic.patient.presentation.theme.StatusConfirmed
import ke.co.smartroundclinic.patient.presentation.theme.StatusPending
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuccess
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuspended
import kotlinx.coroutines.launch
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
    onBookReferredDoctor: (Doctor, String?) -> Unit = { _, _ -> },
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
                // Opening Bookings is exactly what a patient does when a paid-for booking
                // seems to have vanished — re-check any payment still awaiting its booking.
                LaunchedEffect(Unit) { servicesVm.resumePendingBookingIfAny() }
                BookingsListScreen(
                    onAppointmentClick = { backStack.add(BookingAppointmentDetail(it)) },
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                    onBookReferredDoctor = onBookReferredDoctor,
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
                    myRatingOfDoctor = servicesVm.myRatingOfDoctor,
                    hasAlreadyRatedAppointment = servicesVm.hasAlreadyRatedAppointment,
                    isSubmittingRating = servicesVm.isSubmittingRating,
                    onSubmitRating = { rating, comment ->
                        servicesVm.appointmentDetail?.let { servicesVm.submitRating(it.id, it.doctorId, rating, comment) }
                    },
                    onUpdateRating = { rating, comment -> servicesVm.updateMyRating(rating, comment) },
                    onDeleteRating = { servicesVm.deleteMyRating() },
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
                    pendingBookingPayment = servicesVm.pendingBookingPayment,
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
                    onRetryBooking = { date, slotStart -> servicesVm.retryBooking(date, slotStart) },
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
        },
    )
}

@Composable
private fun BookingsListScreen(
    onAppointmentClick: (String) -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onBookReferredDoctor: (Doctor, String?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Appointments", "Referrals")

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
            1 -> ReferralsTab(onBookReferredDoctor = onBookReferredDoctor)
        }
    }
}

// ─── Referrals tab ─────────────────────────────────────────────────────────────

@Composable
private fun ReferralsTab(onBookReferredDoctor: (Doctor, String?) -> Unit) {
    val getReferralHistory: GetReferralHistoryUseCase = koinInject()
    val acceptReferral: AcceptReferralUseCase = koinInject()
    val declineReferral: DeclineReferralUseCase = koinInject()
    val getDoctorById: GetDoctorByIdUseCase = koinInject()
    val snackbarController: SnackbarController = koinInject()

    var referrals by remember { mutableStateOf<List<Referral>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var processingReferralId by remember { mutableStateOf<String?>(null) }
    var referralPendingDecline by remember { mutableStateOf<Referral?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        isLoading = true
        when (val result = getReferralHistory()) {
            is Resource.Success -> referrals = result.data ?: emptyList()
            is Resource.Error -> snackbarController.show(result.message ?: "Failed to load referrals", isError = true)
            else -> {}
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { load() }

    fun bookDoctor(referral: Referral) {
        scope.launch {
            processingReferralId = referral.id
            when (val doctorResult = getDoctorById(referral.receivingDoctorId)) {
                is Resource.Success -> {
                    val doctor = doctorResult.data
                    if (doctor != null) {
                        // The backend only accepts a referralId on the booking request while the
                        // referral is still ACCEPTED and unused — a DECLINED referral's "Book
                        // Anyway" is a normal, untagged booking with this doctor.
                        val referralIdForBooking = referral.id.takeIf { referral.status == ReferralStatus.ACCEPTED }
                        onBookReferredDoctor(doctor, referralIdForBooking)
                    } else {
                        snackbarController.show("Couldn't load Dr. ${referral.receivingDoctorName ?: ""}'s profile", isError = true)
                    }
                }
                is Resource.Error -> snackbarController.show(doctorResult.message ?: "Couldn't load the doctor's profile", isError = true)
                else -> {}
            }
            processingReferralId = null
        }
    }

    fun acceptAndBook(referral: Referral) {
        scope.launch {
            processingReferralId = referral.id
            when (val acceptResult = acceptReferral(referral.id)) {
                is Resource.Success -> {
                    referrals = referrals.map { if (it.id == referral.id) it.copy(status = ReferralStatus.ACCEPTED) else it }
                    when (val doctorResult = getDoctorById(referral.receivingDoctorId)) {
                        is Resource.Success -> {
                            val doctor = doctorResult.data
                            if (doctor != null) onBookReferredDoctor(doctor, referral.id)
                            else snackbarController.show("Referral accepted, but couldn't load Dr. ${referral.receivingDoctorName ?: ""}'s profile", isError = true)
                        }
                        is Resource.Error -> snackbarController.show(doctorResult.message ?: "Referral accepted, but couldn't load the doctor's profile", isError = true)
                        else -> {}
                    }
                }
                is Resource.Error -> snackbarController.show(acceptResult.message ?: "Failed to accept referral", isError = true)
                else -> {}
            }
            processingReferralId = null
        }
    }

    fun decline(referral: Referral) {
        scope.launch {
            processingReferralId = referral.id
            when (val result = declineReferral(referral.id)) {
                is Resource.Success -> referrals = referrals.map { if (it.id == referral.id) it.copy(status = ReferralStatus.DECLINED) else it }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to decline referral", isError = true)
                else -> {}
            }
            processingReferralId = null
        }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { scope.launch { load() } },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isLoading && referrals.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (referrals.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.AutoMirrored.Filled.Forward, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                title = "No referrals yet",
                subtitle = "When another doctor refers you\nto a specialist, it'll show up here.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            ) {
                items(referrals, key = { it.id }) { referral ->
                    ReferralCard(
                        referral = referral,
                        isProcessing = processingReferralId == referral.id,
                        onAcceptAndBook = { acceptAndBook(referral) },
                        onDecline = { referralPendingDecline = referral },
                        onBookDoctor = { bookDoctor(referral) },
                    )
                }
            }
        }
    }

    referralPendingDecline?.let { referral ->
        AlertDialog(
            onDismissRequest = { referralPendingDecline = null },
            title = { Text("Decline this referral?") },
            text = {
                Text(
                    "Dr. ${referral.referringDoctorName ?: "Your doctor"}'s referral to " +
                        "Dr. ${referral.receivingDoctorName ?: "the specialist"} will be declined. You can still book with this doctor later if you change your mind.",
                )
            },
            confirmButton = {
                TextButton(onClick = { decline(referral); referralPendingDecline = null }) {
                    Text("Decline", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { referralPendingDecline = null }) { Text("Cancel") }
            },
        )
    }
}

private val ReferralButtonShape = RoundedCornerShape(5.dp)
private val ReferralButtonHeight = 32.dp

@Composable
private fun ReferralCard(
    referral: Referral,
    isProcessing: Boolean,
    onAcceptAndBook: () -> Unit,
    onDecline: () -> Unit,
    onBookDoctor: () -> Unit,
) {
    // Surface and left accent bar are tinted by status: cream/orange while the referral is
    // still awaiting an answer, green once accepted, red once declined.
    val (surface, accent) = when (referral.status) {
        ReferralStatus.ACCEPTED -> ReferralAcceptedSurface to ReferralAcceptedAccent
        ReferralStatus.DECLINED -> ReferralDeclinedSurface to ReferralDeclinedAccent
        else -> CardBackground to Primary40
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(ShapeCard)
            .background(surface),
    ) {
        Box(
            modifier = Modifier
                .width(7.dp)
                .fillMaxHeight()
                .background(accent),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 21.dp, end = 10.dp, top = 13.dp, bottom = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                ReferralDoctorAvatar(picture = referral.referringDoctorPicture, size = 47)
                Spacer(Modifier.width(13.dp))
                Box(
                    modifier = Modifier.weight(1f).heightIn(min = 47.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "${referral.referringDoctorName?.let { formatDoctorName(it) } ?: "A doctor"} Referred You",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Neutral20,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(8.dp))
                ReferralStatusBadge(status = referral.status)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 60.dp),
            ) {
                ReferralDoctorAvatar(picture = referral.receivingDoctorPicture, size = 28)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append("To ")
                        withStyle(SpanStyle(color = Primary40)) {
                            append(referral.receivingDoctorName?.let { formatDoctorName(it) } ?: "a specialist")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral20,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(18.dp))
            when (referral.status) {
                ReferralStatus.PENDING -> {
                    Row(
                        modifier = Modifier.widthIn(max = 288.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ReferralFilledButton(
                            text = "Accept & Book",
                            onClick = onAcceptAndBook,
                            enabled = !isProcessing,
                            showProgress = isProcessing,
                            container = ReferralAcceptedAccent,
                            modifier = Modifier.weight(1f),
                        )
                        ReferralOutlinedButton(
                            text = "Decline Referral",
                            onClick = onDecline,
                            enabled = !isProcessing,
                            showProgress = false,
                            brush = SolidColor(ReferralDeclineAction),
                            progressColor = ReferralDeclineAction,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                else -> {
                    // Already responded to — accepting/declining doesn't block booking, so a
                    // patient who changes their mind can still go book with this doctor.
                    val isAccepted = referral.status == ReferralStatus.ACCEPTED
                    val brush = if (isAccepted) {
                        SolidColor(ReferralAcceptedAccent)
                    } else {
                        Brush.verticalGradient(listOf(ButtonGradientStart, ButtonGradientEnd))
                    }
                    Row(modifier = Modifier.widthIn(max = 136.dp).fillMaxWidth()) {
                        ReferralOutlinedButton(
                            text = if (isAccepted) "Book Again" else "Book Now",
                            onClick = onBookDoctor,
                            enabled = !isProcessing,
                            showProgress = isProcessing,
                            brush = brush,
                            progressColor = if (isAccepted) ReferralAcceptedAccent else Primary40,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferralFilledButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    showProgress: Boolean,
    container: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(ReferralButtonHeight),
        shape = ReferralButtonShape,
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text(text, style = referralButtonTextStyle(), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ReferralOutlinedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    showProgress: Boolean,
    brush: Brush,
    progressColor: Color,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(ReferralButtonHeight),
        shape = ReferralButtonShape,
        border = BorderStroke(1.dp, brush),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = progressColor)
        } else {
            Text(
                text = text,
                style = referralButtonTextStyle().copy(brush = brush),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun referralButtonTextStyle() =
    MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

@Composable
private fun ReferralDoctorAvatar(picture: String?, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
        contentAlignment = Alignment.Center,
    ) {
        if (picture != null) {
            AsyncImage(
                model = picture,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        } else {
            Icon(Icons.Filled.Person, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size((size * 0.55).dp))
        }
    }
}

@Composable
private fun ReferralStatusBadge(status: ReferralStatus) {
    val (color, label) = when (status) {
        ReferralStatus.ACCEPTED -> ReferralAcceptedAccent to "Accepted"
        ReferralStatus.DECLINED -> ReferralDeclinedAccent to "Declined"
        else -> Primary40 to "Pending"
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), ShapePill)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.2.sp),
            color = color,
        )
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
                appointment.refund?.let { refund ->
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Refund: ${refund.currency} ${refund.amount.toAmountString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        RefundStatusBadge(status = refund.status)
                    }
                }
                // Splits the card in half: appointment date/time above, doctor and View below —
                // matching the divider on the doctor app's consultation card.
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    thickness = 1.dp,
                    color = Neutral20.copy(alpha = 0.26f),
                )
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

@Composable
private fun RefundStatusBadge(status: String) {
    val (fg, label) = when (status.lowercase()) {
        "completed" -> StatusSuccess to "Refunded"
        "failed" -> StatusSuspended to "Refund Failed"
        else -> StatusPending to "Refund Pending"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = fg,
    )
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
