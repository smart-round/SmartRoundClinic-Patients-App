package ke.co.smartroundclinic.patient.presentation.main.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.toDomain
import ke.co.smartroundclinic.patient.domain.model.Notification
import ke.co.smartroundclinic.patient.domain.usecase.notification.GetMyNotificationsUseCase
import ke.co.smartroundclinic.patient.domain.usecase.notification.MarkNotificationReadUseCase
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val getMyNotificationsUseCase: GetMyNotificationsUseCase,
    private val markReadUseCase: MarkNotificationReadUseCase,
) : ViewModel() {

    var notifications by mutableStateOf<List<Notification>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var hasMore by mutableStateOf(false)
        private set

    val unreadCount: Int get() = notifications.count { it.isUnread }

    // Next page to fetch going backwards (newer → older)
    private var nextPageToFetch = 0

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            notifications = emptyList()
            hasMore = false
            nextPageToFetch = 0

            // Page 1 tells us totalPages; also gives oldest items (loaded once here, not repeated later)
            when (val discovery = getMyNotificationsUseCase(page = 1)) {
                is Resource.Success -> {
                    val data = discovery.data?.data ?: run { isLoading = false; return@launch }
                    val total = data.pages.toInt().coerceAtLeast(1)
                    val page1Items = data.items.map { it.toDomain() }

                    if (total == 1) {
                        notifications = page1Items.sortedByDescending { it.createdAt }
                        hasMore = false
                    } else {
                        // Load last page (newest) before showing anything to avoid a flash of old content
                        when (val newest = getMyNotificationsUseCase(page = total)) {
                            is Resource.Success -> {
                                val newestItems = newest.data?.data?.items?.map { it.toDomain() } ?: emptyList()
                                notifications = (page1Items + newestItems).sortedByDescending { it.createdAt }
                                // Middle pages (2 … total-1) load on scroll
                                nextPageToFetch = total - 1
                                hasMore = nextPageToFetch > 1
                            }
                            else -> {
                                notifications = page1Items.sortedByDescending { it.createdAt }
                            }
                        }
                    }
                }
                else -> {}
            }
            isLoading = false
        }
    }

    fun loadMore() {
        // Middle pages: total-1 down to 2 (page 1 already included in initial load)
        if (nextPageToFetch <= 1 || isLoadingMore) return
        viewModelScope.launch {
            isLoadingMore = true
            when (val result = getMyNotificationsUseCase(page = nextPageToFetch)) {
                is Resource.Success -> {
                    val items = result.data?.data?.items?.map { it.toDomain() } ?: emptyList()
                    notifications = (notifications + items).sortedByDescending { it.createdAt }
                    nextPageToFetch--
                    hasMore = nextPageToFetch > 1
                }
                else -> {}
            }
            isLoadingMore = false
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            markReadUseCase(id)
            notifications = notifications.map { n ->
                if (n.id == id) n.copy(status = "READ") else n
            }
        }
    }

    fun markAllRead() {
        val unread = notifications.filter { it.isUnread }
        if (unread.isEmpty()) return
        notifications = notifications.map { it.copy(status = "READ") }
        viewModelScope.launch {
            unread.forEach { markReadUseCase(it.id) }
        }
    }
}
