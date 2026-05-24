package ke.co.smartroundclinic.patient.common

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
) {
    class Loading<T>(message: String = "Loading") : Resource<T>(message = message)
    class Success<T>(data: T?, message: String = "Success") : Resource<T>(data = data, message)
    class Error<T>(message: String, data: T? = null) : Resource<T>(message = message, data = data)
}
