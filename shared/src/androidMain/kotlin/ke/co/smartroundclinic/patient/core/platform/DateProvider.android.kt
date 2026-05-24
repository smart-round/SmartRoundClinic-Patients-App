package ke.co.smartroundclinic.patient.core.platform

import java.util.Calendar

actual fun todayYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
actual fun todayMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
actual fun todayDay(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
