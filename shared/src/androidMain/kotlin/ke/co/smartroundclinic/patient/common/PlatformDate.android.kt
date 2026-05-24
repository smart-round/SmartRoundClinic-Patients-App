package ke.co.smartroundclinic.patient.common

import kotlinx.datetime.LocalDate
import java.util.Calendar

actual fun todayLocalDate(): LocalDate {
    val cal = Calendar.getInstance()
    return LocalDate(
        year = cal.get(Calendar.YEAR),
        monthNumber = cal.get(Calendar.MONTH) + 1,
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
    )
}