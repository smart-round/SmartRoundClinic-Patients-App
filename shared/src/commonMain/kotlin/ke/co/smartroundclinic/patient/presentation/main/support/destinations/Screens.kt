package ke.co.smartroundclinic.patient.presentation.main.support.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SupportTicketList : NavKey
@Serializable data object CreateSupportTicket : NavKey
@Serializable data class SupportChat(val ticketId: String, val ticketNumber: String) : NavKey
