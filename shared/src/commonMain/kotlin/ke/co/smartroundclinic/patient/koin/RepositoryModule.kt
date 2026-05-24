package ke.co.smartroundclinic.patient.koin

import ke.co.smartroundclinic.patient.core.database.AppDatabase
import ke.co.smartroundclinic.patient.data.repository.AppointmentRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.ArticleCategoryLocalRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.ArticleLocalRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.ArticleRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.AuthRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.AvailabilityRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.DatastoreRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.DoctorLocalRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.DoctorRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.ServiceCategoryLocalRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.ServiceCategoryRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.SpecialityLocalRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.SpecialityRepositoryImpl
import ke.co.smartroundclinic.patient.data.repository.UserLocalRepositoryImpl
import ke.co.smartroundclinic.patient.domain.repository.AppointmentRepository
import ke.co.smartroundclinic.patient.domain.repository.ArticleCategoryLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ArticleLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ArticleRepository
import ke.co.smartroundclinic.patient.domain.repository.AuthRepository
import ke.co.smartroundclinic.patient.domain.repository.AvailabilityRepository
import ke.co.smartroundclinic.patient.domain.repository.DatastoreRepository
import ke.co.smartroundclinic.patient.domain.repository.DoctorLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.DoctorRepository
import ke.co.smartroundclinic.patient.domain.repository.ServiceCategoryLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.ServiceCategoryRepository
import ke.co.smartroundclinic.patient.domain.repository.SpecialityLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.SpecialityRepository
import ke.co.smartroundclinic.patient.domain.repository.UserLocalRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<DatastoreRepository> { DatastoreRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserLocalRepository> { UserLocalRepositoryImpl(get<AppDatabase>().userDao) }
    single<AvailabilityRepository> { AvailabilityRepositoryImpl(get()) }
    single<AppointmentRepository> { AppointmentRepositoryImpl(get()) }
    single<SpecialityRepository> { SpecialityRepositoryImpl(get()) }
    single<SpecialityLocalRepository> { SpecialityLocalRepositoryImpl(get<AppDatabase>().specialityDao) }
    single<ArticleRepository> { ArticleRepositoryImpl(get()) }
    single<ArticleLocalRepository> { ArticleLocalRepositoryImpl(get<AppDatabase>().articleDao) }
    single<ArticleCategoryLocalRepository> { ArticleCategoryLocalRepositoryImpl(get<AppDatabase>().articleCategoryDao) }
    single<DoctorRepository> { DoctorRepositoryImpl(get()) }
    single<DoctorLocalRepository> { DoctorLocalRepositoryImpl(get<AppDatabase>().doctorDao) }
    single<ServiceCategoryRepository> { ServiceCategoryRepositoryImpl(get()) }
    single<ServiceCategoryLocalRepository> { ServiceCategoryLocalRepositoryImpl(get<AppDatabase>().serviceCategoryDao) }
}
