package ke.co.smartroundclinic.patient.presentation.main.Services.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.Primary40
import ke.co.smartroundclinic.patient.presentation.theme.Primary90

@Composable
fun DoctorsByCategoryScreen(
    categoryName: String,
    categoryDescription: String = "",
    categoryIconUrl: String? = null,
    doctors: List<Doctor>,
    isLoading: Boolean = false,
    currentPage: Int = 1,
    totalPages: Int = 1,
    onLoadPage: (Int) -> Unit = {},
    onDoctorClick: (Doctor) -> Unit,
    onBookNow: (Doctor) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(vertical = 4.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = if (isLoading) "Loading..." else "${doctors.size} specialists available",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                if (categoryDescription.isNotBlank()) {
                    item(key = "header") {
                        SpecialityHeaderCard(
                            name = categoryName,
                            description = categoryDescription,
                            iconUrl = categoryIconUrl,
                        )
                    }
                }
                items(doctors) { doctor ->
                    DoctorListCard(
                        doctor = doctor,
                        onCardClick = { onDoctorClick(doctor) },
                        onBookNow = { onBookNow(doctor) },
                    )
                }
                if (totalPages > 1) {
                    item(key = "pagination") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            repeat(totalPages) { index ->
                                val page = index + 1
                                val isSelected = page == currentPage
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                        )
                                        .then(
                                            if (!isSelected && !isLoading)
                                                Modifier.clickable { onLoadPage(page) }
                                            else Modifier,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isLoading && isSelected) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White,
                                        )
                                    } else {
                                        Text(
                                            text = "$page",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = if (isSelected) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorListCard(
    doctor: Doctor,
    onCardClick: () -> Unit,
    onBookNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Primary40),
        )
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onCardClick),
            shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp),
                    )
                    if (doctor.profilePicture != null) {
                        AsyncImage(
                            model = doctor.profilePicture,
                            contentDescription = doctor.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doctor.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (doctor.specialization != null) {
                        Text(
                            text = doctor.specialization,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (doctor.facilityName != null) {
                        Text(
                            text = doctor.facilityName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = run { val t = (doctor.averageRating * 10).toInt(); "${t / 10}.${t % 10}" },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = " (${doctor.totalReviews})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                        .clickable(onClick = onBookNow)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Book Now",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialityHeaderCard(
    name: String,
    description: String,
    iconUrl: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary90),
                contentAlignment = Alignment.Center,
            ) {
                if (iconUrl != null) {
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(36.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.MedicalServices,
                        contentDescription = null,
                        tint = Primary40,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val Doctor.displayName: String
    get() {
        val stripped = name.removePrefix("Dr. ").removePrefix("Dr.").removePrefix("Dr ").trim()
        return "Dr. ${stripped.split(" ").take(2).joinToString(" ")}"
    }
