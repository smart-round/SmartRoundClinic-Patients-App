package ke.co.smartroundclinic.patient.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.request.UpdatePersonalInformationReq
import ke.co.smartroundclinic.patient.data.remote.dto.response.PersonalInformationResponse
import ke.co.smartroundclinic.patient.domain.repository.PersonalInformationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class PersonalInformationRepositoryImpl(private val client: HttpClient) : PersonalInformationRepository {

    override suspend fun get(): Resource<PersonalInformationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.get("patient/personal-information").body<PersonalInformationResponse>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }

    override suspend fun update(
        weight: Double?,
        weightIn: String?,
        height: Double?,
        heightIn: String?,
        bloodGroup: String?,
        maritalStatus: String?,
        allergies: List<String>?,
        chronicConditions: List<String>?,
        currentMedications: List<String>?,
    ): Resource<PersonalInformationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.put("patient/personal-information") {
                    setBody(
                        UpdatePersonalInformationReq(
                            weight = weight,
                            weightIn = weightIn,
                            height = height,
                            heightIn = heightIn,
                            bloodGroup = bloodGroup,
                            maritalStatus = maritalStatus,
                            allergies = allergies,
                            chronicConditions = chronicConditions,
                            currentMedications = currentMedications,
                        )
                    )
                }.body<PersonalInformationResponse>()
                if (response.status) Resource.Success(data = response, message = response.message)
                else Resource.Error(message = response.message)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
}
