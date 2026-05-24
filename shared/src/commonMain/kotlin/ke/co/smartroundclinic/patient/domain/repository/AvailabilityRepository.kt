package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.domain.model.CalendarView

interface AvailabilityRepository {
    suspend fun getAvailableSlots(doctorId: String, date: String): Resource<List<String>>
    suspend fun getCalendarView(doctorId: String, view: String, date: String): Resource<CalendarView>
}
