package ke.co.smartroundclinic.patient.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Specialization(
    val id: String, // 6a08a70a8751f92f85e39bb2
    val specializationId: String, // 69dbb5ac73f215116ff7fdad
    val specializationName: String, // Dermatology
    val subSpecializationId: String?, // 69dbb5ac73f215116ff7fdc7
    val subSpecializationName: String? // General OB/GYN
)