package ke.co.smartroundclinic.patient.presentation.main.profile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryItem
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetPaymentHistoryUseCase
import ke.co.smartroundclinic.patient.presentation.main.bookings.ui.PaymentStatusChip
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.card_payment
import ke.co.smartroundclinic.patient.generated.resources.mpesa
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

/**
 * Payments — moved out from under Bookings' tab row and re-hosted under Profile (below
 * Personal Information), per user direction. Content/logic is a straight extraction of the
 * former `PaymentsTab`/`PaymentCard` from BookingsRoot.kt; only the header changes (was a
 * shared tab-row body, now its own back-button screen matching Profile's other sub-screens).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentsListScreen(
    onPaymentClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val getHistory: GetPaymentHistoryUseCase = koinInject()
    var payments by remember { mutableStateOf<List<GetPaymentHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(false) }
    var nextPageToFetch by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val nearBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - 3
        }
    }

    LaunchedEffect(nearBottom) {
        if (nearBottom && hasMore && !isLoadingMore) {
            isLoadingMore = true
            when (val result = getHistory(page = nextPageToFetch)) {
                is Resource.Success -> {
                    val items = result.data?.data?.items ?: emptyList()
                    payments = (payments + items).sortedByDescending { it.createdAt }
                    nextPageToFetch--
                    hasMore = nextPageToFetch > 1
                }
                else -> {}
            }
            isLoadingMore = false
        }
    }

    suspend fun loadInitial() {
        isLoading = true
        payments = emptyList()
        hasMore = false
        nextPageToFetch = 0

        when (val discovery = getHistory(page = 1)) {
            is Resource.Success -> {
                val data = discovery.data?.data ?: run { isLoading = false; return }
                val total = data.pages.coerceAtLeast(1)
                val page1Items = data.items

                if (total == 1) {
                    payments = page1Items.sortedByDescending { it.createdAt }
                } else {
                    // Load newest page before revealing content — avoids flash of oldest payments
                    when (val newest = getHistory(page = total)) {
                        is Resource.Success -> {
                            val newestItems = newest.data?.data?.items ?: emptyList()
                            payments = (page1Items + newestItems).sortedByDescending { it.createdAt }
                            nextPageToFetch = total - 1
                            hasMore = nextPageToFetch > 1
                        }
                        else -> payments = page1Items.sortedByDescending { it.createdAt }
                    }
                }
            }
            else -> {}
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { loadInitial() }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = "Payments",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { scope.launch { loadInitial() } },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            if (isLoading && payments.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().navigationBarsPadding(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Receipt, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(16.dp))
                        Text(text = "No payments yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Your payment history will\nappear here after your first booking.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                ) {
                    items(payments, key = { it.id }) { payment ->
                        PaymentCard(
                            payment = payment,
                            onClick = { onPaymentClick(payment.id) },
                        )
                    }
                    when {
                        isLoadingMore -> item(key = "loading_more") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                        !hasMore -> item(key = "end_of_list") {
                            Text(
                                text = "· All payments loaded ·",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentCard(payment: GetPaymentHistoryItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    val painter = if (payment.paymentMethod.contains("MPESA", ignoreCase = true) ||
                        payment.paymentMethod.contains("M-PESA", ignoreCase = true))
                        painterResource(Res.drawable.mpesa)
                    else
                        painterResource(Res.drawable.card_payment)
                    Image(
                        painter = painter,
                        contentDescription = payment.paymentMethod,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = payment.paymentMethod,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = formatPaymentDate(payment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${payment.currency} ${payment.amount.toAmountString()}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    PaymentStatusChip(status = payment.status)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val ref = payment.mpesaReference?.takeIf { it.isNotBlank() }
                    ?: payment.invoiceId?.takeIf { it.isNotBlank() }
                    ?: "—"
                InfoChip(label = "Ref", value = ref)
                payment.netAmount?.let { InfoChip(label = "Net", value = "${payment.currency} $it") }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun Double.toAmountString(): String {
    val whole = toLong()
    val cents = ((this - whole) * 100).toLong()
    return "$whole.${cents.toString().padStart(2, '0')}"
}

private fun formatPaymentDate(iso: String): String = try {
    val date = iso.substringBefore("T")
    val time = iso.substringAfter("T").substringBefore(".").substringBefore("Z")
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val min = parts.getOrElse(1) { "00" }
    val ampm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    "$date · $h:$min $ampm"
} catch (e: Exception) { iso }
