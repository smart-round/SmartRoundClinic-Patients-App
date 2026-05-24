package ke.co.smartroundclinic.patient.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ke.co.smartroundclinic.patient.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberSvgPainter(path: String): Painter {
    var bytes by remember(path) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(path) { bytes = Res.readBytes(path) }
    val context = LocalPlatformContext.current
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(bytes)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
    )
}
