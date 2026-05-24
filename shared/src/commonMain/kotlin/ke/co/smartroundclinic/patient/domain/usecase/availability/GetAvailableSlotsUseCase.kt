package ke.co.smartroundclinic.patient.domain.usecase.availability

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.repository.AvailabilityRepository

class GetAvailableSlotsUseCase(private val repository: AvailabilityRepository) {
    suspend operator fun invoke(doctorId: String, date: String): Resource<List<String>> =
        repository.getAvailableSlots(doctorId, date)
}
