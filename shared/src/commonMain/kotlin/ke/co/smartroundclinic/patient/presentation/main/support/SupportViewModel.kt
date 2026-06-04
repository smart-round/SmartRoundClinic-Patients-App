package ke.co.smartroundclinic.patient.presentation.main.support

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.domain.model.IssueCategory
import ke.co.smartroundclinic.patient.domain.model.SupportTicket
import ke.co.smartroundclinic.patient.domain.usecase.support.CreateSupportTicketUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.GetIssueCategoriesUseCase
import ke.co.smartroundclinic.patient.domain.usecase.support.GetMyTicketsUseCase
import kotlinx.coroutines.launch

class SupportViewModel(
    private val getMyTicketsUseCase: GetMyTicketsUseCase,
    private val getIssueCategoriesUseCase: GetIssueCategoriesUseCase,
    private val createTicketUseCase: CreateSupportTicketUseCase,
    private val snackbarController: SnackbarController,
) : ViewModel() {

    var tickets by mutableStateOf<List<SupportTicket>>(emptyList())
        private set

    var categories by mutableStateOf<List<IssueCategory>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isCreating by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val ticketsResult = getMyTicketsUseCase()
            val categoriesResult = getIssueCategoriesUseCase()
            isLoading = false
            if (ticketsResult is Resource.Success) tickets = ticketsResult.data ?: emptyList()
            if (categoriesResult is Resource.Success) categories = categoriesResult.data ?: emptyList()
        }
    }

    fun createTicket(
        categoryId: String,
        title: String,
        description: String,
        complainantName: String,
        complainantEmail: String,
        onSuccess: (SupportTicket) -> Unit,
    ) {
        viewModelScope.launch {
            isCreating = true
            val result = createTicketUseCase(categoryId, title, description, complainantName, complainantEmail)
            isCreating = false
            when (result) {
                is Resource.Success -> result.data?.let { ticket ->
                    tickets = listOf(ticket) + tickets
                    snackbarController.show("Ticket ${ticket.ticketNumber} created")
                    onSuccess(ticket)
                }
                is Resource.Error -> snackbarController.show(result.message ?: "Failed to create ticket", isError = true)
                else -> Unit
            }
        }
    }
}
