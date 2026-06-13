package ke.co.smartroundclinic.patient.presentation.main.support

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.core.snackbar.SnackbarController
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
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

    var currentPage by mutableStateOf(1)
        private set

    var totalPages by mutableStateOf(1)
        private set

    init {
        load()
    }

    fun load() {
        loadTicketsPage(currentPage)
        viewModelScope.launch {
            val categoriesResult = getIssueCategoriesUseCase()
            if (categoriesResult is Resource.Success) categories = categoriesResult.data ?: emptyList()
        }
    }

    fun loadTicketsPage(page: Int) {
        viewModelScope.launch {
            isLoading = true
            when (val result = getMyTicketsUseCase(page)) {
                is Resource.Success -> {
                    val data = result.data?.data ?: return@launch
                    tickets = data.items.map { it.toDomain() }
                    currentPage = data.page
                    totalPages = data.pages.toInt().coerceAtLeast(1)
                }
                else -> {}
            }
            isLoading = false
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
