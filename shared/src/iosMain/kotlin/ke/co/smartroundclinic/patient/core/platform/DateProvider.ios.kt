package ke.co.smartroundclinic.patient.core.platform

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun todayYear(): Int {
    val comps = NSCalendar.currentCalendar.components(NSCalendarUnitYear, NSDate())
    return comps.year.toInt()
}

actual fun todayMonth(): Int {
    val comps = NSCalendar.currentCalendar.components(NSCalendarUnitMonth, NSDate())
    return comps.month.toInt()
}

actual fun todayDay(): Int {
    val comps = NSCalendar.currentCalendar.components(NSCalendarUnitDay, NSDate())
    return comps.day.toInt()
}
