package ke.co.smartroundclinic.patient.presentation.main.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.presentation.main.Services.ServicesViewModel
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.AppointmentDetailsScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.BookAppointmentScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.DoctorProfileScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.DoctorsByCategoryScreen
import ke.co.smartroundclinic.patient.presentation.main.articles.ui.ArticleDetailScreen
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.AllDoctors
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.HomeAppointmentDetails
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.HomeBookAppointment
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.HomeDoctorArticleDetail
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.HomeDoctorProfile
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.HomeDoctorsBySpeciality
import ke.co.smartroundclinic.patient.core.notification.NotificationDeepLink
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.HomeScreen
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.Notifications
import ke.co.smartroundclinic.patient.presentation.main.home.destinations.UserProfile
import ke.co.smartroundclinic.patient.presentation.main.home.ui.AllDoctorsScreen
import ke.co.smartroundclinic.patient.presentation.main.home.ui.HomeScreen
import ke.co.smartroundclinic.patient.presentation.main.notifications.NotificationsScreen
import ke.co.smartroundclinic.patient.presentation.main.notifications.NotificationsViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.ProfileRoot
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(HomeScreen) }
    val isAtRoot = backStack.size == 1
    val servicesVm: ServicesViewModel = koinViewModel()
    val homeVm: HomeViewModel = koinViewModel()
    val notificationsVm: NotificationsViewModel = koinViewModel()

    SideEffect { onAtRootChanged(isAtRoot) }

    // Navigate to Notifications when a push notification is tapped
    LaunchedEffect(Unit) {
        NotificationDeepLink.pending.collect { pending ->
            if (pending) {
                NotificationDeepLink.consume()
                if (!backStack.contains(Notifications)) {
                    backStack.add(Notifications)
                }
            }
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeScreen> {
                HomeScreen(
                    onSeeAllDoctors = { backStack.add(AllDoctors) },
                    onSeeMoreServices = onNavigateToServices,
                    onProfileClick = { backStack.add(UserProfile) },
                    onNotificationClick = { backStack.add(Notifications) },
                    onDoctorClick = { doctor ->
                        servicesVm.cacheDoctor(doctor)
                        backStack.add(HomeDoctorProfile(doctor.id))
                    },
                    onSpecialityClick = { speciality ->
                        backStack.add(HomeDoctorsBySpeciality(speciality.id, speciality.title))
                    },
                    unreadNotificationCount = notificationsVm.unreadCount,
                )
            }
            entry<Notifications> {
                NotificationsScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<UserProfile> {
                ProfileRoot(
                    onSignOut = onSignOut,
                    onExitProfile = { backStack.removeLastOrNull() },
                )
            }
            entry<HomeDoctorsBySpeciality> { dest ->
                LaunchedEffect(dest.specialityId) {
                    servicesVm.loadDoctorsBySpeciality(dest.specialityId)
                }
                DoctorsByCategoryScreen(
                    categoryName = dest.specialityName,
                    doctors = servicesVm.specialityDoctors,
                    isLoading = servicesVm.isLoadingDoctors,
                    onDoctorClick = { doctor ->
                        servicesVm.cacheDoctor(doctor)
                        backStack.add(HomeDoctorProfile(doctor.id))
                    },
                    onBookNow = { doctor ->
                        servicesVm.cacheDoctor(doctor)
                        backStack.add(HomeBookAppointment(doctor.id))
                    },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<AllDoctors> {
                AllDoctorsScreen(
                    doctors = homeVm.doctors,
                    specialities = homeVm.specialities,
                    isLoading = homeVm.isLoading,
                    onDoctorClick = { doctor ->
                        servicesVm.cacheDoctor(doctor)
                        backStack.add(HomeDoctorProfile(doctor.id))
                    },
                    onBookNow = { doctor ->
                        servicesVm.cacheDoctor(doctor)
                        backStack.add(HomeBookAppointment(doctor.id))
                    },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<HomeDoctorProfile> { dest ->
                val doctor = servicesVm.doctorById(dest.doctorId)
                if (doctor != null) {
                    DoctorProfileScreen(
                        doctor = doctor,
                        onBookNow = { backStack.add(HomeBookAppointment(dest.doctorId)) },
                        onBack = { backStack.removeLastOrNull() },
                        onArticleClick = { article ->
                            servicesVm.selectedArticle = article
                            backStack.add(HomeDoctorArticleDetail)
                        },
                    )
                }
            }
            entry<HomeDoctorArticleDetail> {
                val article = servicesVm.selectedArticle
                if (article != null) {
                    ArticleDetailScreen(
                        article = article,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            }
            entry<HomeBookAppointment> { dest ->
                // Auto-navigate to appointment details once booking is confirmed
                LaunchedEffect(servicesVm.bookedAppointment) {
                    val id = servicesVm.bookedAppointment?.id ?: return@LaunchedEffect
                    servicesVm.clearBookingState()
                    backStack.removeLastOrNull() // pop HomeBookAppointment
                    backStack.add(HomeAppointmentDetails(id))
                }

                val doctor = servicesVm.doctorById(dest.doctorId) ?: return@entry
                BookAppointmentScreen(
                    doctor = doctor,
                    calendarView = servicesVm.calendarView,
                    availableSlots = servicesVm.availableSlots,
                    isLoadingSlots = servicesVm.isLoadingSlots,
                    isPreBooking = servicesVm.isPreBooking,
                    isBooking = servicesVm.isBooking,
                    preBookData = servicesVm.preBookData,
                    preBookError = servicesVm.preBookError,
                    bookedAppointmentId = servicesVm.bookedAppointment?.id,
                    bookingError = servicesVm.bookingError,
                    onLoadCalendar = { yearMonth -> servicesVm.loadCalendarView(dest.doctorId, yearMonth) },
                    onLoadSlots = { date -> servicesVm.loadSlots(dest.doctorId, date) },
                    isRebooking = dest.previousAppointmentId != null,
                    previousAppointmentId = dest.previousAppointmentId,
                    onPayNow = { date, slotStart ->
                        servicesVm.preBookAppointment(
                            doctorId = dest.doctorId,
                            date = date,
                            slotStart = slotStart,
                            isRebooking = dest.previousAppointmentId != null,
                            previousAppointmentId = dest.previousAppointmentId,
                        )
                    },
                    onPaymentDone = { servicesVm.confirmBookingAfterPayment() },
                    onDismissCheckout = { servicesVm.dismissCheckout() },
                    onViewBooking = { appointmentId ->
                        servicesVm.clearBookingState()
                        backStack.add(HomeAppointmentDetails(appointmentId))
                    },
                    onDismissResult = { servicesVm.clearBookingState() },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<HomeAppointmentDetails> { dest ->
                AppointmentDetailsScreen(
                    appointmentId = dest.appointmentId,
                    appointment = servicesVm.appointmentDetail,
                    doctor = servicesVm.appointmentDetail?.let { servicesVm.doctorById(it.doctorId) },
                    onLoad = { id -> servicesVm.loadAppointmentDetail(id) },
                    onBack = { backStack.removeLastOrNull() },
                    onRebook = { doctor, previousAppointmentId ->
                        servicesVm.cacheDoctor(doctor)
                        backStack.add(HomeBookAppointment(doctor.id, previousAppointmentId))
                    },
                )
            }
        },
    )
}
