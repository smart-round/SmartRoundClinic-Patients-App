package ke.co.smartroundclinic.patient.presentation.main.bookings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import ke.co.smartroundclinic.patient.common.Resource
import ke.co.smartroundclinic.patient.data.remote.dto.response.GetPaymentHistoryByIdData
import ke.co.smartroundclinic.patient.domain.usecase.payments.GetPaymentHistoryByIdUseCase
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.card_payment
import ke.co.smartroundclinic.patient.generated.resources.mpesa
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import org.jetbrains.compose.resources.painterResource
import ke.co.smartroundclinic.patient.presentation.theme.StatusConfirmed
import ke.co.smartroundclinic.patient.presentation.theme.StatusPending
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuccess
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuspended
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import androidx.compose.ui.platform.LocalClipboardManager

@Composable
fun PaymentHistoryDetailScreen(
    paymentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val getById: GetPaymentHistoryByIdUseCase = koinInject()
    var payment by remember { mutableStateOf<GetPaymentHistoryByIdData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(paymentId) {
        isLoading = true
        when (val result = getById(paymentId)) {
            is Resource.Success -> payment = result.data?.data
            else -> {}
        }
        isLoading = false
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(vertical = 4.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Payment Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(vertical = 16.dp),
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return
        }

        val data = payment ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Payment details not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Amount hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                        RoundedCornerShape(20.dp),
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Payment method icon with status badge overlay
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            val painter = if (data.paymentMethod.contains("MPESA", ignoreCase = true) ||
                                data.paymentMethod.contains("M-PESA", ignoreCase = true))
                                painterResource(Res.drawable.mpesa)
                            else
                                painterResource(Res.drawable.card_payment)
                            Image(
                                painter = painter,
                                contentDescription = data.paymentMethod,
                                modifier = Modifier.size(44.dp),
                            )
                        }
                        val (statusIcon, statusTint) = when (data.status.uppercase()) {
                            "COMPLETED" -> Icons.Filled.CheckCircle to StatusSuccess
                            "FAILED", "CANCELLED" -> Icons.Filled.Error to StatusSuspended
                            else -> Icons.Filled.Schedule to StatusPending
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(imageVector = statusIcon, contentDescription = null, tint = statusTint, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${data.currency} ${data.amount.toAmountString()}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Spacer(Modifier.height(4.dp))
                    PaymentStatusChip(status = data.status)
                }
            }

            // Transaction details
            DetailCard(title = "Transaction Details") {
                DetailRow("Payment Method", data.paymentMethod)
                data.mpesaReference?.let { DetailRow("M-Pesa Reference", it, copyable = true) }
                data.invoiceId?.let { DetailRow("Invoice ID", it, copyable = true) }
                DetailRow("Transaction Ref", data.transactionRef, copyable = true)
                data.account?.let { DetailRow("Account", it, copyable = true) }
                DetailRow("Date", formatPaymentDate(data.createdAt))
            }

            // Breakdown
            DetailCard(title = "Amount Breakdown") {
                DetailRow("Gross Amount", "${data.currency} ${data.amount.toAmountString()}")
                data.charges?.let { DetailRow("Charges", "${data.currency} $it") }
                DetailRow("Platform Fee", "${data.currency} ${data.platformFee.toAmountString()}")
                data.netAmount?.let {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    DetailRow("Net Amount", "${data.currency} $it", bold = true)
                }
            }

            // References
            DetailCard(title = "References") {
                data.appointmentId?.let { DetailRow("Appointment ID", it.takeLast(12), copyable = true) }
                DetailRow("Payment ID", data.id, copyable = true)
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    bold: Boolean = false,
    copyable: Boolean = false,
) {
    val clipboard = LocalClipboardManager.current
    var justCopied by remember { mutableStateOf(false) }

    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(1500)
            justCopied = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f),
        )
        Row(
            modifier = Modifier.weight(0.58f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = if (bold)
                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (copyable) {
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(value))
                        justCopied = true
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (justCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = if (justCopied) "Copied" else "Copy",
                        tint = if (justCopied) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentStatusChip(status: String) {
    val (bg, fg) = when (status.uppercase()) {
        "COMPLETED" -> StatusSuccess.copy(alpha = 0.2f) to StatusSuccess
        "FAILED", "CANCELLED" -> StatusSuspended.copy(alpha = 0.2f) to StatusSuspended
        "PENDING", "PROCESSING" -> StatusPending.copy(alpha = 0.2f) to StatusPending
        else -> StatusConfirmed.copy(alpha = 0.2f) to StatusConfirmed
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
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
    "$date  $h:$min $ampm"
} catch (e: Exception) { iso }
