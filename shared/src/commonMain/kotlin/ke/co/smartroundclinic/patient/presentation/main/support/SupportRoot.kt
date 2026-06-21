package ke.co.smartroundclinic.patient.presentation.main.support

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.presentation.main.support.destinations.CreateSupportTicket
import ke.co.smartroundclinic.patient.presentation.main.support.destinations.SupportChat
import ke.co.smartroundclinic.patient.presentation.main.support.destinations.SupportTicketList
import ke.co.smartroundclinic.patient.presentation.main.support.ui.CreateSupportTicketScreen
import ke.co.smartroundclinic.patient.presentation.main.support.ui.SupportChatScreen
import ke.co.smartroundclinic.patient.presentation.main.support.ui.SupportTicketListScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SupportRoot(
    user: User?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    pendingTicketId: String? = null,
    onPendingNavigated: () -> Unit = {},
) {
    val backStack = retain { mutableStateListOf<NavKey>(SupportTicketList) }
    val vm: SupportViewModel = koinViewModel()

    LaunchedEffect(pendingTicketId, vm.tickets) {
        if (!pendingTicketId.isNullOrBlank() && vm.tickets.isNotEmpty()) {
            val ticket = vm.tickets.find { it.id == pendingTicketId }
            if (ticket != null) {
                backStack.removeAll { it is SupportChat }
                backStack.add(SupportChat(ticket.id, ticket.ticketNumber))
                onPendingNavigated()
            }
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) backStack.removeLastOrNull()
            else onBack()
        },
        entryProvider = entryProvider {
            entry<SupportTicketList> {
                SupportTicketListScreen(
                    tickets = vm.tickets,
                    isLoading = vm.isLoading,
                    currentPage = vm.currentPage,
                    totalPages = vm.totalPages,
                    onBack = onBack,
                    onCreateTicket = { backStack.add(CreateSupportTicket) },
                    onTicketClick = { ticket -> backStack.add(SupportChat(ticket.id, ticket.ticketNumber)) },
                    onRefresh = { vm.load() },
                    onLoadPage = { vm.loadTicketsPage(it) },
                )
            }
            entry<CreateSupportTicket> {
                CreateSupportTicketScreen(
                    categories = vm.categories,
                    isCreating = vm.isCreating,
                    onBack = { backStack.removeLastOrNull() },
                    onSubmit = { categoryId, title, description ->
                        vm.createTicket(
                            categoryId = categoryId,
                            title = title,
                            description = description,
                            complainantName = user?.fullName ?: "",
                            complainantEmail = user?.email ?: "",
                        ) { ticket ->
                            backStack.removeLastOrNull()
                            backStack.add(SupportChat(ticket.id, ticket.ticketNumber))
                        }
                    },
                )
            }
            entry<SupportChat> { dest ->
                val chatVm: SupportChatViewModel = koinViewModel(key = dest.ticketId, parameters = { parametersOf(dest.ticketId) })
                SupportChatScreen(
                    ticketNumber = dest.ticketNumber,
                    currentUserId = user?.id ?: "",
                    vm = chatVm,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
