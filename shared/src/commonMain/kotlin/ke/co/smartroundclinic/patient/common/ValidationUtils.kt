package ke.co.smartroundclinic.patient.common

fun String.isValidEmail(): Boolean =
    contains("@") && substringAfter("@").contains(".") && length > 5

fun String.isValidPassword(): Boolean = length >= 8
