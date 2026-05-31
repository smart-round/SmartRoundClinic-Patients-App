package ke.co.smartroundclinic.patient.presentation.main.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ke.co.smartroundclinic.patient.domain.model.Doctor
import ke.co.smartroundclinic.patient.domain.model.Speciality
import ke.co.smartroundclinic.patient.domain.model.User
import ke.co.smartroundclinic.patient.generated.resources.Res
import ke.co.smartroundclinic.patient.generated.resources.notification
import ke.co.smartroundclinic.patient.presentation.main.home.HomeViewModel
import ke.co.smartroundclinic.patient.presentation.theme.CardBackground
import ke.co.smartroundclinic.patient.presentation.theme.GradientEnd
import ke.co.smartroundclinic.patient.presentation.theme.GradientStart
import ke.co.smartroundclinic.patient.presentation.theme.SearchBarOverlay
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuspended
import ke.co.smartroundclinic.patient.presentation.theme.Secondary40
import ke.co.smartroundclinic.patient.presentation.theme.Secondary90
import ke.co.smartroundclinic.patient.presentation.theme.ServiceTileColors
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSeeMoreServices: () -> Unit = {},
    onSeeAllDoctors: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onDoctorClick: (Doctor) -> Unit = {},
    onSpecialityClick: (Speciality) -> Unit = {},
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val user by viewModel.user.collectAsState()
    val doctors = viewModel.doctors
    val specialities = viewModel.specialities
    val isRefreshing = viewModel.isLoading
    val visibleSpecialities = remember(specialities) { specialities.take(8) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                HomeHeader(
                    user = user,
                    onProfileClick = onProfileClick,
                    onNotificationClick = onNotificationClick,
                    unreadCount = unreadNotificationCount,
                )
                Spacer(Modifier.height(20.dp))
                ConsultDoctorSection(doctors = doctors, onSeeAll = onSeeAllDoctors, onDoctorClick = onDoctorClick)
                Spacer(Modifier.height(20.dp))
                ExploreServicesSection(
                    specialities = visibleSpecialities,
                    onSeeMore = onSeeMoreServices,
                    onSpecialityClick = onSpecialityClick,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader(
    user: User?,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    unreadCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(brush = Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onProfileClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                    if (user?.profilePicture != null) {
                        AsyncImage(
                            model = user.profilePicture,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                val firstName = user?.fullName?.split(" ")?.firstOrNull() ?: "there"
                Text(
                    text = "Hello $firstName 👋",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )

                Box {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            painter = painterResource(Res.drawable.notification),
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .padding(top = 2.dp, end = 2.dp)
                                .clip(CircleShape)
                                .background(StatusSuspended),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Your health, seamlessly\nmanaged in one place.",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConsultDoctorSection(
    doctors: List<Doctor>,
    onSeeAll: () -> Unit,
    onDoctorClick: (Doctor) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Consult A Doctor",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onSeeAll,
                ),
            )
        }
        Spacer(Modifier.height(12.dp))
        if (doctors.isEmpty()) {
            DoctorCarouselPlaceholder()
        } else {
            HorizontalMultiBrowseCarousel(
                state = rememberCarouselState { doctors.size },
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                preferredItemWidth = 220.dp,
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) { i ->
                val fraction = carouselItemDrawInfo.size /
                    carouselItemDrawInfo.maxSize.coerceAtLeast(1f)
                DoctorPhotoCard(
                    doctor = doctors[i],
                    visibilityFraction = fraction,
                    onClick = { onDoctorClick(doctors[i]) },
                    modifier = Modifier
                        .height(250.dp)
                        .maskClip(MaterialTheme.shapes.medium),
                )
            }
        }
    }
}

private fun String.stripDrPrefix() =
    removePrefix("Dr. ").removePrefix("Dr.").removePrefix("Dr ").trim()

@Composable
private fun DoctorPhotoCard(
    doctor: Doctor,
    visibilityFraction: Float = 1f,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val displayName = "Dr. ${doctor.name.stripDrPrefix().split(" ").take(2).joinToString(" ")}"

    // Overlay alpha: invisible when item is <55% of full width, fully visible at 100%
    val overlayAlpha by animateFloatAsState(
        targetValue = if (visibilityFraction.isNaN()) 1f
                      else ((visibilityFraction - 0.55f) / 0.45f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 180),
        label = "overlayAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        // Background — photo or branded gradient when no picture
        if (doctor.profilePicture != null) {
            AsyncImage(
                model = doctor.profilePicture,
                contentDescription = doctor.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(72.dp),
                )
            }
        }

        // Bottom scrim + info — slides up and fades in
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    alpha = overlayAlpha
                    translationY = (1f - overlayAlpha) * 24f
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                    )
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                )
                if (doctor.specialization != null) {
                    Text(
                        text = doctor.specialization,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                    )
                }
                if (doctor.facilityName != null) {
                    Text(
                        text = doctor.facilityName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                    )
                }
            }
        }

        // Rating pill — fades in and slides down from top-right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    alpha = overlayAlpha
                    translationY = -(1f - overlayAlpha) * 16f
                }
                .padding(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 7.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = (doctor.averageRating * 10).roundToInt().let { r -> "${r / 10}.${r % 10}" },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun DoctorCarouselPlaceholder(modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(4) {
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Secondary90),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Secondary40.copy(alpha = 0.3f))
                            )
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(110.dp).height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Secondary40.copy(alpha = 0.3f)),
                        )
                        Box(
                            modifier = Modifier
                                .width(75.dp).height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Secondary40.copy(alpha = 0.2f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreServicesSection(
    specialities: List<Speciality>,
    onSeeMore: () -> Unit,
    onSpecialityClick: (Speciality) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Explore Specialities",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        if (specialities.isEmpty()) {
            ServiceGridPlaceholder()
        } else {
            val rows = specialities.size / 4 + if (specialities.size % 4 != 0) 1 else 0
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth().height((rows * 96).dp),
                userScrollEnabled = false,
            ) {
                itemsIndexed(specialities) { index, speciality ->
                    SpecialityItem(
                        speciality = speciality,
                        fallbackColor = ServiceTileColors[index % ServiceTileColors.size],
                        onClick = { onSpecialityClick(speciality) },
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onSeeMore,
            shape = RoundedCornerShape(50),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = "See More",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SpecialityItem(
    speciality: Speciality,
    fallbackColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tileColor = speciality.color.parseHexPastel() ?: fallbackColor
    Column(
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tileColor),
            contentAlignment = Alignment.Center,
        ) {
            if (speciality.iconUrl != null) {
                AsyncImage(
                    model = speciality.iconUrl,
                    contentDescription = speciality.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(34.dp),
                )
            } else {
                Text(
                    text = speciality.title.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF444444),
                )
            }
        }
        Text(
            text = speciality.title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ServiceGridPlaceholder(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.fillMaxWidth().height(192.dp),
        userScrollEnabled = false,
    ) {
        items(8) { index ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ServiceTileColors[index % ServiceTileColors.size]),
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CardBackground),
                )
            }
        }
    }
}

private fun String.parseHexPastel(): Color? = try {
    val hex = removePrefix("#").let { if (it.length == 3) it.map { c -> "$c$c" }.joinToString("") else it }
    if (hex.length != 6) return null
    val r = hex.substring(0, 2).toInt(16) / 255f
    val g = hex.substring(2, 4).toInt(16) / 255f
    val b = hex.substring(4, 6).toInt(16) / 255f
    Color(red = 0.75f + r * 0.25f, green = 0.75f + g * 0.25f, blue = 0.75f + b * 0.25f)
} catch (e: Exception) {
    null
}
