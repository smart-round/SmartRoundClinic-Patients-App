package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorArticlesRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorBySpecializationRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorByIdRes
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorsProfileRes

import ke.co.smartroundclinic.patient.data.remote.dto.response.GetDoctorsRecommendationRes

interface DoctorRepository {
    suspend fun getRecommendedDoctors(): Resource<GetDoctorsRecommendationRes>
    suspend fun getDoctorBySpecialization(specializationId:String,page:Int, size:Int): Resource<GetDoctorBySpecializationRes>
    suspend fun getDoctorProfile(id:String): Resource<GetDoctorsProfileRes>
    suspend fun getDoctorArticles(id: String, page: Int,size: Int): Resource<GetDoctorArticlesRes>
    suspend fun getDoctorById(doctorId: String): Resource<GetDoctorByIdRes>
}
