package ke.co.smartroundclinic.patient.presentation.main.support.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.domain.model.SupportTicket
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90
import ke.co.smartroundclinic.patient.presentation.theme.ShapeCard
import ke.co.smartroundclinic.patient.presentation.theme.Tertiary40
import ke.co.smartroundclinic.patient.presentation.theme.Error40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SupportTicketListScreen(
    tickets: List<SupportTicket>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onCreateTicket: () -> Unit,
    onTicketClick: (SupportTicket) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateTicket,
                containerColor = Primary40,
                contentColor = Color.White,
                modifier = Modifier.navigationBarsPadding(),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Ticket", style = MaterialTheme.typography.labelLarge)
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding()
                    .padding(vertical = 4.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.align(Alignment.Center).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Support", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("My support tickets", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                }
            }

            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (isLoading && tickets.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary40)
                    }
                } else if (tickets.isEmpty()) {
                    Box(Modifier.fillMaxSize().navigationBarsPadding(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Primary90), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, tint = Primary40, modifier = Modifier.size(36.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("No Tickets Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(6.dp))
                            Text("Create a new ticket to get help from our support team.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(tickets, key = { it.id }) { ticket ->
                            TicketCard(ticket = ticket, onClick = { onTicketClick(ticket) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: SupportTicket, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (statusColor, statusLabel) = when (ticket.status.uppercase()) {
        "OPEN" -> Primary40 to "Open"
        "IN_PROGRESS" -> Tertiary40 to "In Progress"
        "CLOSED" -> MaterialTheme.colorScheme.onSurfaceVariant to "Closed"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to ticket.status
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(ticket.ticketNumber, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Primary40)
                Spacer(Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(statusColor.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(ticket.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Spacer(Modifier.height(2.dp))
            Text(ticket.categoryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(formatDate(ticket.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

private fun formatDate(iso: String): String = try {
    val date = iso.substringBefore("T")
    val time = iso.substringAfter("T").substringBefore(".").substringBefore("Z")
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val min = parts.getOrElse(1) { "00" }
    val ampm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    "$date · $h:$min $ampm"
} catch (e: Exception) { iso }
