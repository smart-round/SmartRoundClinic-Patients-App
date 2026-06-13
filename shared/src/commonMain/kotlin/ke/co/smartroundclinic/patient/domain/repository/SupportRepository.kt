package ke.co.smartroundclinic.patient.domain.repository

import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetMyTicketsRes
import ke.co.smartroundclinic.patient.domain.model.IssueCategory
import ke.co.smartroundclinic.patient.domain.model.SupportChatMessage
import ke.co.smartroundclinic.patient.domain.model.SupportTicket

interface SupportRepository {
    suspend fun getCategories(): Resource<List<IssueCategory>>
    suspend fun createTicket(
        categoryId: String,
        title: String,
        description: String,
        complainantName: String,
        complainantEmail: String,
    ): Resource<SupportTicket>
    suspend fun getMyTickets(page: Int = 1, size: Int = 20): Resource<GetMyTicketsRes>
    suspend fun getChatHistory(ticketId: String): Resource<List<SupportChatMessage>>
    suspend fun uploadChatFile(ticketId: String, bytes: ByteArray, fileName: String, contentType: String): Resource<SupportChatMessage>
}
