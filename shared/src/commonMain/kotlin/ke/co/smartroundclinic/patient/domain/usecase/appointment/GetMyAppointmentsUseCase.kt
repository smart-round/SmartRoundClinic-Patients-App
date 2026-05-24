package ke.co.smartroundclinic.patient.domain.usecase.appointment

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.Appointment
import ke.co.smartroundclinic.patient.domain.repository.AppointmentLocalRepository
import ke.co.smartroundclinic.patient.domain.repository.AppointmentRepository

class GetMyAppointmentsUseCase(
    private val remoteRepository: AppointmentRepository,
    private val localRepository: AppointmentLocalRepository,
) {
    suspend operator fun invoke(): Resource<List<Appointment>> {
        return when (val result = remoteRepository.getMyAppointments()) {
            is Resource.Success -> {
                val data = result.data ?: emptyList()
                if (data.isNotEmpty()) localRepository.upsertAll(data)
                result
            }
            is Resource.Error -> {
                val cached = localRepository.getAll()
                if (cached.isNotEmpty()) Resource.Success(data = cached) else result
            }
            else -> result
        }
    }

    suspend fun getCached(): List<Appointment> = localRepository.getAll()
}
