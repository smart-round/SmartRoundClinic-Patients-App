package ke.co.smartroundclinic.patient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect val notificationPlatform: String