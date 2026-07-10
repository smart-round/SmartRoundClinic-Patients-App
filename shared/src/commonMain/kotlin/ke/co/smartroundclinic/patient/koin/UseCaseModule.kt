package ke.co.smartroundclinic.patient.koin

import ke.co.smartroundclinic.patient.domain.usecase.appointment.BookAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.CreateSupportTicketUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.GetIssueCategoriesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.GetMyTicketsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.GetSupportChatHistoryUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.UploadChatFileUseCase
import ke.co.smartroundclinic.patient.presentation.main.support.SupportChatViewModel
import ke.co.smartroundclinic.patient.presentation.main.support.SupportViewModel
import ke.co.smartroundclinic.patient.domain.usecase.notification.GetMyNotificationsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.notification.MarkNotificationReadUseCase
import ke.co.smartroundclinic.patient.domain.usecase.notification.RegisterDeviceTokenUseCase
import ke.co.smartroundclinic.patient.presentation.main.notifications.NotificationsViewModel
import ke.co.smartroundclinic.patient.domain.usecase.consultation.DeleteConversationThreadUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.GetMergedConsultationHistoryUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.JoinConsultationCallUseCase
import ke.co.smartroundclinic.patient.domain.usecase.consultation.ListConversationThreadsUseCase
import ke.co.smartroundclinic.patient.presentation.main.chat.ConsultationViewModel
import ke.co.smartroundclinic.patient.domain.usecase.appointment.CancelAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetAppointmentUseCase
import ke.co.smartroundclinic.patient.domain.usecase.appointment.GetMyAppointmentsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.articles.GetArticleCategoriesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.articles.GetLiveArticlesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.GetUserUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.RefreshTokenUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.RemoveProfilePictureUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.RequestPasswordResetUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.ResendAccountUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.ResendPasswordResetOtpUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.SignInUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.SignOutUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.SignUpUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.UpdatePasswordUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.UpdateProfileUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.UploadProfilePictureUseCase
import ke.co.smartroundclinic.patient.domain.usecase.auth.VerifyAccountUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetAvailableSlotsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.availability.GetCalendarViewUseCase
import ke.co.smartroundclinic.patient.domain.usecase.datastore.GetKeyUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorArticlesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorProfileUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetDoctorsBySpecializationUseCase
import ke.co.smartroundclinic.patient.domain.usecase.doctor.GetRecommendedDoctorsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.servicecategory.GetServiceCategoriesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetPaymentHistoryByIdUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetPaymentHistoryUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetStkPushStatusUseCase
import ke.co.smartroundclinic.patient.domain.usecase.payments.StkPushPreBookingUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialitiesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.speciality.GetSpecialityPricingUseCase
import ke.co.smartroundclinic.patient.domain.usecase.datastore.ObserveKeyUseCase
import ke.co.smartroundclinic.patient.domain.usecase.datastore.SetKeyUseCase
import ke.co.smartroundclinic.patient.domain.usecase.medicalrecord.GetMedicalRecordUseCase
import ke.co.smartroundclinic.patient.domain.usecase.medicalrecord.GetMyMedicalHistoryUseCase
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.CreatePersonalInformationUseCase
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.GetPersonalInformationUseCase
import ke.co.smartroundclinic.patient.domain.usecase.personalinfo.UpdatePersonalInformationUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.DeleteDoctorRatingUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.GetDoctorRatingsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.RateDoctorUseCase
import ke.co.smartroundclinic.patient.domain.usecase.rating.UpdateDoctorRatingUseCase
import ke.co.smartroundclinic.patient.presentation.auth.ForgotPasswordViewModel
import ke.co.smartroundclinic.patient.presentation.auth.SignInViewModel
import ke.co.smartroundclinic.patient.presentation.main.articles.ArticlesViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.MedicalBioViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.MedicalHistoryViewModel
import ke.co.smartroundclinic.patient.presentation.main.profile.ProfileViewModel
import ke.co.smartroundclinic.patient.presentation.main.Services.DoctorsProfileViewModel
import ke.co.smartroundclinic.patient.presentation.main.Services.ServicesViewModel
import ke.co.smartroundclinic.patient.presentation.main.home.HomeViewModel
import ke.co.smartroundclinic.patient.presentation.onboarding.OnboardingScreenViewModel
import ke.co.smartroundclinic.patient.presentation.signup.AccountVerificationViewModel
import ke.co.smartroundclinic.patient.presentation.signup.SignUpFilesViewModel
import ke.co.smartroundclinic.patient.presentation.signup.SignUpFormViewModel
import ke.co.smartroundclinic.patient.presentation.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    // Datastore use cases
    single { ObserveKeyUseCase(get()) }
    single { SetKeyUseCase(get()) }
    single { GetKeyUseCase(get()) }

    // Auth use cases
    single { SignInUseCase(get(), get()) }
    single { SignUpUseCase(get()) }
    single { VerifyAccountUseCase(get()) }
    single { ResendAccountUseCase(get()) }
    single { RequestPasswordResetUseCase(get()) }
    single { ResendPasswordResetOtpUseCase(get()) }
    single { UpdatePasswordUseCase(get()) }
    single { RefreshTokenUseCase(get(), get()) }
    single { GetUserUseCase(get(), get()) }
    single { SignOutUseCase(get(), get(), get()) }
    single { UpdateProfileUseCase(get(), get()) }
    single { UploadProfilePictureUseCase(get()) }
    single { RemoveProfilePictureUseCase(get()) }

    // Availability use cases
    single { GetAvailableSlotsUseCase(get()) }
    single { GetCalendarViewUseCase(get()) }

    single { RegisterDeviceTokenUseCase(get()) }
    single { GetMyNotificationsUseCase(get()) }
    single { MarkNotificationReadUseCase(get()) }

    // Consultation use cases
    single { JoinConsultationCallUseCase(get()) }
    single { ListConversationThreadsUseCase(get()) }
    single { GetMergedConsultationHistoryUseCase(get()) }
    single { DeleteConversationThreadUseCase(get()) }

    // Payment use cases
    single { StkPushPreBookingUseCase(get()) }
    single { GetStkPushStatusUseCase(get()) }
    single { GetPaymentHistoryUseCase(get()) }
    single { GetPaymentHistoryByIdUseCase(get()) }

    // Appointment use cases
    single { BookAppointmentUseCase(get()) }
    single { GetMyAppointmentsUseCase(get(), get()) }
    single { GetAppointmentUseCase(get()) }
    single { CancelAppointmentUseCase(get()) }

    // Speciality use cases
    single { GetSpecialitiesUseCase(get(), get()) }
    single { GetSpecialityPricingUseCase(get()) }

    // Doctor use cases
    single { GetRecommendedDoctorsUseCase(get(), get()) }
    single { GetDoctorsBySpecializationUseCase(get()) }
    single { GetDoctorProfileUseCase(get()) }
    single { GetDoctorArticlesUseCase(get()) }

    // Service category use cases
    single { GetServiceCategoriesUseCase(get(), get()) }

    // Article use cases
    single { GetLiveArticlesUseCase(get(), get()) }
    single { GetArticleCategoriesUseCase(get(), get()) }

    // ViewModels
    viewModel { SplashViewModel(get(), get()) }
    viewModel { OnboardingScreenViewModel(get(), get()) }
    viewModel { SignInViewModel(get(), get(), get(), get(), get()) }
    viewModel { ForgotPasswordViewModel(get(), get(), get(), get()) }
    viewModel { AccountVerificationViewModel(get(), get(), get()) }
    viewModel { SignUpFilesViewModel() }
    viewModel { SignUpFormViewModel(get(), get()) }
    viewModel { ArticlesViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { DoctorsProfileViewModel(get(), get(), get(), get()) }
    viewModel { ServicesViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ConsultationViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { NotificationsViewModel(get(), get()) }
    viewModel { MedicalBioViewModel(get(), get(), get(), get(), get()) }
    viewModel { MedicalHistoryViewModel(get()) }

    // Medical record use cases
    single { GetMedicalRecordUseCase(get()) }
    single { GetMyMedicalHistoryUseCase(get()) }

    // Personal information use cases
    single { GetPersonalInformationUseCase(get()) }
    single { CreatePersonalInformationUseCase(get()) }
    single { UpdatePersonalInformationUseCase(get()) }

    // Rating use cases
    single { RateDoctorUseCase(get()) }
    single { UpdateDoctorRatingUseCase(get()) }
    single { DeleteDoctorRatingUseCase(get()) }
    single { GetDoctorRatingsUseCase(get()) }

    // Support use cases + ViewModels
    single { GetIssueCategoriesUseCase(get()) }
    single { CreateSupportTicketUseCase(get()) }
    single { GetMyTicketsUseCase(get()) }
    single { UploadChatFileUseCase(get()) }
    single { GetSupportChatHistoryUseCase(get()) }
    viewModel { SupportViewModel(get(), get(), get(), get()) }
    viewModel { (ticketId: String) -> SupportChatViewModel(ticketId, get(), get(), get(), get(), get()) }
}
