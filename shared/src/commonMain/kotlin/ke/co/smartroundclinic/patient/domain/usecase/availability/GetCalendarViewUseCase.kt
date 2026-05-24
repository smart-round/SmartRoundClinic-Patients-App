package ke.co.smartroundclinic.patient.domain.usecase.availability

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CalendarView
import ke.co.smartroundclinic.patient.domain.repository.AvailabilityRepository

class GetCalendarViewUseCase(private val repository: AvailabilityRepository) {
    suspend operator fun invoke(
        doctorId: String,
        view: String = "month",
        date: String,
    ): Resource<CalendarView> = repository.getCalendarView(doctorId, view, date)
}
