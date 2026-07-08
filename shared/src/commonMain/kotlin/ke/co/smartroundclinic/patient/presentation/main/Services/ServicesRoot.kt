package ke.co.smartroundclinic.patient.presentation.main.Services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.presentation.main.Services.destinations.AppointmentDetails
import ke.co.smartroundclinic.patient.presentation.main.Services.destinations.BookAppointment
import ke.co.smartroundclinic.patient.presentation.main.Services.destinations.DoctorArticleDetail
import ke.co.smartroundclinic.patient.presentation.main.Services.destinations.DoctorProfile
import ke.co.smartroundclinic.patient.presentation.main.Services.destinations.DoctorsByCategory
import ke.co.smartroundclinic.patient.presentation.main.Services.destinations.ServicesList
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.AppointmentDetailsScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.BookAppointmentScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.DoctorProfileScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.DoctorsByCategoryScreen
import ke.co.smartroundclinic.patient.presentation.main.Services.ui.ServiceCategoriesScreen
import ke.co.smartroundclinic.patient.presentation.main.articles.ui.ArticleDetailScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ServicesRoot(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(ServicesList) }
    val isAtRoot = backStack.size == 1
    SideEffect { onAtRootChanged(isAtRoot) }

    val vm: ServicesViewModel = koinViewModel()

    LaunchedEffect(vm.pendingDoctor) {
        val doctor = vm.pendingDoctor ?: return@LaunchedEffect
        vm.cacheDoctor(doctor)
        backStack.clear()
        backStack.add(ServicesList)
        backStack.add(DoctorProfile(doctor.id))
        vm.pendingDoctor = null
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<ServicesList> {
                ServiceCategoriesScreen(
                    specialities = vm.specialities,
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                    onSpecialityClick = { speciality ->
                        backStack.add(
                            DoctorsByCategory(
                                categoryId = speciality.id,
                                categoryName = speciality.title,
                                categoryDescription = speciality.description,
                                categoryIconUrl = speciality.iconUrl,
                            ),
                        )
                    },
                )
            }

            entry<DoctorsByCategory> { dest ->
                LaunchedEffect(dest.categoryId) {
                    vm.loadDoctorsBySpeciality(dest.categoryId)
                }
                DoctorsByCategoryScreen(
                    categoryName = dest.categoryName,
                    categoryDescription = dest.categoryDescription,
                    categoryIconUrl = dest.categoryIconUrl,
                    doctors = vm.specialityDoctors,
                    isLoading = vm.isLoadingDoctors,
                    currentPage = vm.doctorsCurrentPage,
                    totalPages = vm.doctorsTotalPages,
                    onLoadPage = { vm.loadDoctorsPage(dest.categoryId, it) },
                    onDoctorClick = { doctor -> backStack.add(DoctorProfile(doctor.id)) },
                    onBookNow = { doctor -> backStack.add(DoctorProfile(doctor.id)) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            entry<DoctorProfile> { dest ->
                val doctor = vm.doctorById(dest.doctorId)
                if (doctor != null) {
                    DoctorProfileScreen(
                        doctor = doctor,
                        onBookNow = { backStack.add(BookAppointment(dest.doctorId)) },
                        onBack = { backStack.removeLastOrNull() },
                        onArticleClick = { article ->
                            vm.selectedArticle = article
                            backStack.add(DoctorArticleDetail)
                        },
                    )
                }
            }

            entry<DoctorArticleDetail> {
                val article = vm.selectedArticle
                if (article != null) {
                    ArticleDetailScreen(
                        article = article,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            }

            entry<BookAppointment> { dest ->
                BookAppointmentScreen(
                    doctor = vm.doctorById(dest.doctorId) ?: return@entry,
                    calendarView = vm.calendarView,
                    availableSlots = vm.availableSlots,
                    isLoadingSlots = vm.isLoadingSlots,
                    isStkInitiating = vm.isStkInitiating,
                    isBooking = vm.isBooking,
                    stkPushData = vm.stkPushData,
                    stkPollState = vm.stkPollState,
                    stkError = vm.stkError,
                    bookedAppointmentId = vm.bookedAppointment?.id,
                    bookingError = vm.bookingError,
                    isRebooking = dest.previousAppointmentId != null,
                    previousAppointmentId = dest.previousAppointmentId,
                    onLoadCalendar = { yearMonth -> vm.loadCalendarView(dest.doctorId, yearMonth) },
                    onLoadSlots = { date -> vm.loadSlots(dest.doctorId, date) },
                    onInitiateStkPush = { date, slotStart, phoneNumber ->
                        vm.initiateStkPush(
                            doctorId = dest.doctorId,
                            date = date,
                            slotStart = slotStart,
                            phoneNumber = phoneNumber,
                            isRebooking = dest.previousAppointmentId != null,
                            previousAppointmentId = dest.previousAppointmentId,
                        )
                    },
                    onDismissStkPush = { vm.dismissStkPush() },
                    onViewBooking = { appointmentId ->
                        vm.clearBookingState()
                        backStack.removeLastOrNull()
                        backStack.add(AppointmentDetails(appointmentId))
                    },
                    onDismissResult = { vm.clearBookingState() },
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            entry<AppointmentDetails> { dest ->
                AppointmentDetailsScreen(
                    appointmentId = dest.appointmentId,
                    appointment = vm.appointmentDetail,
                    doctor = vm.appointmentDetail?.let { vm.doctorById(it.doctorId) },
                    medicalRecord = vm.medicalRecord,
                    isLoadingMedicalRecord = vm.isLoadingMedicalRecord,
                    onLoad = { id -> vm.loadAppointmentDetail(id) },
                    onBack = { backStack.removeLastOrNull() },
                    onRebook = { doctor, previousAppointmentId ->
                        vm.cacheDoctor(doctor)
                        backStack.add(BookAppointment(doctor.id, previousAppointmentId))
                    },
                    onCancel = { id, reason -> vm.cancelAppointment(id, reason) },
                    isCancelling = vm.isCancelling,
                    myRatingOfDoctor = vm.myRatingOfDoctor,
                    hasAlreadyRatedAppointment = vm.hasAlreadyRatedAppointment,
                    isSubmittingRating = vm.isSubmittingRating,
                    onSubmitRating = { rating, comment ->
                        vm.appointmentDetail?.let { vm.submitRating(it.id, it.doctorId, rating, comment) }
                    },
                    onUpdateRating = { rating, comment -> vm.updateMyRating(rating, comment) },
                    onDeleteRating = { vm.deleteMyRating() },
                )
            }
        },
    )
}
