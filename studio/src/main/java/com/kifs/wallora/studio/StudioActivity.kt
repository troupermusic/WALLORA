package com.kifs.wallora.studio

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Black = Color(0xFF050505)
private val SurfaceDark = Color(0xFF151515)
private val SurfaceLight = Color(0xFF222222)
private val Yellow = Color(0xFFFFD900)
private val Muted = Color(0xFF999999)

class StudioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudioApp() }
    }
}

@Composable
private fun StudioApp() {
    var page by remember { mutableStateOf("Overview") }
    var mediaUri by remember { mutableStateOf<Uri?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("Untitled wallpaper") }
    var previewMode by remember { mutableStateOf("Lock") }
    var selectedDevice by remember { mutableStateOf("Galaxy") }
    var fillFrame by remember { mutableStateOf(true) }
    var speed by remember { mutableFloatStateOf(1f) }
    var muted by remember { mutableStateOf(true) }
    var timelinePosition by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(0L) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var wallpaperTitle by remember { mutableStateOf("") }
    var wallpaperDescription by remember { mutableStateOf("") }
    var wallpaperCategory by remember { mutableStateOf("Wallpaper") }
    var uploadWallpaperUri by remember { mutableStateOf<Uri?>(null) }
    var uploadPreviewUri by remember { mutableStateOf<Uri?>(null) }
    var uploadWallpaperIsVideo by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val wallpaperUploadPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                uploadWallpaperUri = uri
                uploadWallpaperIsVideo =
                    context.contentResolver.getType(uri)?.startsWith("video/") == true
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }
        }

    val phonePreviewPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                uploadPreviewUri = uri
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }
        }


    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            mediaUri = uri
            isVideo = context.contentResolver.getType(uri)?.startsWith("video/") == true
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            timelinePosition = 0f
            currentPosition = 0L
            duration = 0L
            page = "Editor"
        }
    }

    val player = remember(mediaUri, isVideo) {
        if (mediaUri != null && isVideo) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(mediaUri!!))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
        } else null
    }

    DisposableEffect(player) {
        onDispose { player?.release() }
    }

    LaunchedEffect(player) {
        while (player != null) {
            currentPosition = player.currentPosition.coerceAtLeast(0L)
            duration = player.duration.takeIf { it > 0 } ?: 0L
            timelinePosition = currentPosition.toFloat()
            delay(250)
        }
    }

    LaunchedEffect(player, speed) {
        player?.setPlaybackParameters(PlaybackParameters(speed))
    }

    LaunchedEffect(player, muted) {
        player?.volume = if (muted) 0f else 1f
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Black,
            surface = SurfaceDark,
            primary = Yellow,
            onPrimary = Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Black,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (page != "Editor") NavigationBar(containerColor = SurfaceDark) {
                    listOf("Overview", "Editor", "Catalog", "Team").forEach { item ->
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            icon = {
                                when (item) {
                                    "Overview" -> Icon(Icons.Default.Dashboard, null)
                                    "Editor" -> Icon(Icons.Default.Edit, null)
                                    "Catalog" -> Icon(Icons.Default.Collections, null)
                                    else -> Icon(Icons.Default.Groups, null)
                                }
                            },
                            label = { Text(item, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Yellow,
                                selectedTextColor = Yellow,
                                indicatorColor = Color(0xFF302900),
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (page) {
                "Editor" -> EditorPage(
                    projectName = projectName,
                    onProjectName = { projectName = it },
                    mediaUri = mediaUri,
                    isVideo = isVideo,
                    player = player,
                    previewMode = previewMode,
                    onPreviewMode = { previewMode = it },
                    selectedDevice = selectedDevice,
                    onSelectedDevice = { selectedDevice = it },
                    fillFrame = fillFrame,
                    onFillFrame = { fillFrame = it },
                    speed = speed,
                    onSpeed = { speed = it },
                    muted = muted,
                    onMuted = { muted = it },
                    position = timelinePosition,
                    duration = duration,
                    onPosition = { timelinePosition = it },
                    onSeek = { player?.seekTo(it.toLong()) },
                    onImport = { mediaPicker.launch(arrayOf("image/*", "video/*")) },
                    onExport = {
                        scope.launch {
                            snackbar.showSnackbar(
                                "Export engine is the next stage. No file has been exported yet."
                            )
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
                "Overview" -> OverviewPage(
                    onOpenEditor = { page = "Editor" },
                    onImport = { mediaPicker.launch(arrayOf("image/*", "video/*")) },
                    modifier = Modifier.padding(padding)
                )
                "Catalog" -> CatalogPage(Modifier.padding(padding))
                "Team" -> TeamPage(Modifier.padding(padding))
                "Publish" -> PublishPage(
                    title = wallpaperTitle,
                    onTitle = { wallpaperTitle = it },
                    description = wallpaperDescription,
                    onDescription = { wallpaperDescription = it },
                    category = wallpaperCategory,
                    onCategory = { wallpaperCategory = it },
                    wallpaperUri = uploadWallpaperUri,
                    previewUri = uploadPreviewUri,
                    wallpaperIsVideo = uploadWallpaperIsVideo,
                    onSelectWallpaper = {
                        wallpaperUploadPicker.launch(arrayOf("image/*", "video/*"))
                    },
                    onRemoveWallpaper = {
                        uploadWallpaperUri = null
                        uploadWallpaperIsVideo = false
                    },
                    onSelectPreview = {
                        phonePreviewPicker.launch(arrayOf("image/*"))
                    },
                    onRemovePreview = { uploadPreviewUri = null },
                    onBack = { page = "Editor" },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun EditorPage(
    projectName: String,
    onProjectName: (String) -> Unit,
    mediaUri: Uri?,
    isVideo: Boolean,
    player: ExoPlayer?,
    previewMode: String,
    onPreviewMode: (String) -> Unit,
    selectedDevice: String,
    onSelectedDevice: (String) -> Unit,
    fillFrame: Boolean,
    onFillFrame: (Boolean) -> Unit,
    speed: Float,
    onSpeed: (Float) -> Unit,
    muted: Boolean,
    onMuted: (Boolean) -> Unit,
    position: Float,
    duration: Long,
    onPosition: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Original") }
    var activePanel by remember { mutableStateOf("Adjust") }
    var exposure by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var temperature by remember { mutableFloatStateOf(0f) }
    var highlights by remember { mutableFloatStateOf(0f) }
    var shadows by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val devices = listOf(
        "iPhone Island", "iPhone Notch", "Galaxy", "Pixel", "Tecno",
        "Xiaomi", "Huawei", "OnePlus", "OPPO/Vivo", "Motorola",
        "Nothing", "Generic"
    )
    val bezel = when (selectedDevice) {
        "iPhone Island", "iPhone Notch" -> 5.dp
        "Nothing" -> 4.dp
        "Generic" -> 2.dp
        else -> 6.dp
    }
    val frameCorner = when (selectedDevice) {
        "iPhone Island", "iPhone Notch" -> 36.dp
        "Pixel" -> 29.dp
        "Nothing" -> 25.dp
        "Generic" -> 18.dp
        else -> 31.dp
    }
    val screenCorner = (frameCorner - bezel - 3.dp).coerceAtLeast(12.dp)
    val cutoutWidth = when (selectedDevice) {
        "iPhone Island" -> 64.dp
        "iPhone Notch" -> 88.dp
        "Generic" -> 0.dp
        else -> 12.dp
    }
    val cutoutHeight = when (selectedDevice) {
        "iPhone Island", "iPhone Notch" -> 18.dp
        "Generic" -> 0.dp
        else -> 12.dp
    }

    Column(
        modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("WALLORA STUDIO", color = Yellow, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold)
                Text(projectName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.Default.Undo, "Undo", tint = Muted) }
                IconButton(onClick = {}) { Icon(Icons.Default.Redo, "Redo", tint = Muted) }
                Button(
                    onClick = onExport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Yellow, contentColor = Black
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Export")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Lock", "Home", "Live").forEach { mode ->
                FilterChip(
                    selected = previewMode == mode,
                    onClick = { onPreviewMode(mode) },
                    label = { Text(mode) },
                    leadingIcon = {
                        if (mode == "Lock") Icon(Icons.Default.Lock, null,
                            Modifier.size(15.dp))
                        else if (mode == "Home") Icon(Icons.Default.Home, null,
                            Modifier.size(15.dp))
                        else Icon(Icons.Default.MotionPhotosOn, null,
                            Modifier.size(15.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Yellow,
                        selectedLabelColor = Black,
                        selectedLeadingIconColor = Black
                    )
                )
                Spacer(Modifier.width(5.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "DEVICE PREVIEW LAB",
            color = Yellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(5.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            devices.forEach { device ->
                FilterChip(
                    selected = selectedDevice == device,
                    onClick = { onSelectedDevice(device) },
                    label = { Text(device, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Yellow,
                        selectedLabelColor = Black
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.width(260.dp).height((LocalConfiguration.current.screenHeightDp.dp - 440.dp).coerceIn(300.dp, 430.dp))
                    .clip(RoundedCornerShape(frameCorner))
                    .background(Color(0xFF101010))
                    .padding(bezel)
                    .clip(RoundedCornerShape(screenCorner))
                    .background(Color(0xFF080808)),
                contentAlignment = Alignment.Center
            ) {
                if (mediaUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Wallpaper, null,
                            tint = Yellow, modifier = Modifier.size(42.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Your preview appears here",
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(5.dp))
                        Text("Import an image or video",
                            color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onImport,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Yellow, contentColor = Black
                            )
                        ) { Text("Import media") }
                    }
                } else if (isVideo && player != null) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = player
                                useController = true
                                resizeMode = if (fillFrame)
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                else AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        update = {
                            it.player = player
                            it.resizeMode = if (fillFrame)
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            else AspectRatioFrameLayout.RESIZE_MODE_FIT
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = mediaUri,
                        contentDescription = "Wallpaper preview",
                        contentScale = if (fillFrame) ContentScale.Crop else ContentScale.Fit,
                        colorFilter = ColorFilter.colorMatrix(
                            adjustedWallpaperFilter(
                                selectedFilter, exposure, contrast, saturation,
                                temperature, highlights, shadows
                            )
                        ),
                        modifier = Modifier.fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoomChange, _ ->
                                    zoom = (zoom * zoomChange).coerceIn(1f, 5f)
                                    if (zoom > 1f) {
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                    )
                }
                if (selectedDevice != "Generic") {
                    val cutoutShape = when (selectedDevice) {
                        "iPhone Island" -> RoundedCornerShape(50)
                        "iPhone Notch" -> RoundedCornerShape(
                            topStart = 0.dp, topEnd = 0.dp,
                            bottomStart = 12.dp, bottomEnd = 12.dp
                        )
                        else -> RoundedCornerShape(50)
                    }
                    Box(
                        Modifier.align(Alignment.TopCenter)
                            .padding(top = 7.dp)
                            .size(cutoutWidth, cutoutHeight)
                            .clip(cutoutShape)
                            .background(Color.Black)
                    )
                }
                if (mediaUri != null && previewMode != "Live") {
                    Column(
                        Modifier.align(Alignment.TopCenter).padding(
                            top = if (selectedDevice == "iPhone Island" ||
                                selectedDevice == "iPhone Notch") 32.dp else 22.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("9:41", color = Color.White, fontSize = 30.sp,
                            fontWeight = FontWeight.Light)
                        Text("Friday, November 20", color = Color.White,
                            fontSize = 10.sp)
                    }
                }
                if (mediaUri != null && previewMode == "Home") {
                    Row(
                        Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        repeat(4) {
                            Box(Modifier.size(27.dp).clip(RoundedCornerShape(9.dp))
                                .background(Color(0xAAFFFFFF)))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (isVideo) "VIDEO TIMELINE" else "WALLPAPER CANVAS",
                color = Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(if (mediaUri == null) "No media" else if (isVideo) "Video clip" else "Image layer",
                color = Muted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(8.dp))
        Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Movie, null, tint = Yellow)
                    Spacer(Modifier.width(8.dp))
                    Text(if (mediaUri == null) "Empty timeline"
                        else if (isVideo) "Video track 01" else "Image track 01",
                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Text(formatTime(duration), color = Muted, fontSize = 11.sp)
                }
                if (isVideo && duration > 0) {
                    Slider(
                        value = position.coerceIn(0f, duration.toFloat()),
                        onValueChange = onPosition,
                        onValueChangeFinished = { onSeek(position) },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Yellow,
                            activeTrackColor = Yellow
                        )
                    )
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(position.toLong()), color = Muted,
                            fontSize = 11.sp)
                        Text(formatTime(duration), color = Muted, fontSize = 11.sp)
                    }
                } else {
                    Box(Modifier.fillMaxWidth().height(5.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(SurfaceLight))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, null, tint = Muted,
                        modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Clip controls", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (isVideo) "Video" else "Still image",
                        color = Yellow, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(15.dp))
        Text("EDITING TOOLS", color = Yellow, fontSize = 11.sp,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))

        val editorTools = listOf(
            Triple("Import", Icons.Default.AddPhotoAlternate, onImport),
            Triple("Frame", Icons.Default.Crop, { onFillFrame(!fillFrame) }),
            Triple("Speed", Icons.Default.Speed, {
                onSpeed(if (speed >= 2f) 0.5f else speed + 0.5f)
            }),
            Triple("Sound", if (muted) Icons.Default.VolumeOff
                else Icons.Default.VolumeUp, { onMuted(!muted) }),
            Triple("Loop", Icons.Default.Repeat, {
                player?.repeatMode =
                    if (player?.repeatMode == Player.REPEAT_MODE_ONE)
                        Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
            }),
            Triple("Adjust", Icons.Default.Tune, { activePanel = "Adjust" }),
            Triple("Filters", Icons.Default.ColorLens, { activePanel = "Filters" }),
            Triple("Export", Icons.Default.FileUpload, onExport)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            editorTools.chunked(4).forEach { rowTools ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowTools.forEach { tool ->
                        Surface(
                            onClick = { tool.third() },
                            color = SurfaceDark,
                            shape = RoundedCornerShape(11.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                Modifier.height(58.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(tool.second, null, tint = Yellow,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.height(3.dp))
                                Text(tool.first, fontSize = 10.sp,
                                    color = Color.White)
                            }
                        }
                    }
                    repeat(4 - rowTools.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(7.dp))
        if (activePanel == "Adjust") {
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ADJUST IMAGE", color = Yellow, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold)
                        TextButton(onClick = {
                            exposure = 0f
                            contrast = 1f
                            saturation = 1f
                            temperature = 0f
                            highlights = 0f
                            shadows = 0f
                        }) {
                            Text("Reset", color = Yellow, fontSize = 11.sp)
                        }
                    }
                    AdjustmentControl("Exposure", exposure, -1f, 1f, "0.0") { exposure = it }
                    AdjustmentControl("Contrast", contrast, 0f, 2f, "0.00") { contrast = it }
                    AdjustmentControl("Saturation", saturation, 0f, 2f, "0.00") { saturation = it }
                    AdjustmentControl("Temperature", temperature, -1f, 1f, "0.0") { temperature = it }
                    AdjustmentControl("Highlights", highlights, -1f, 1f, "0.0") { highlights = it }
                    AdjustmentControl("Shadows", shadows, -1f, 1f, "0.0") { shadows = it }
                    Text(
                        "Sharpness requires pixel-level image processing and is not enabled in this preview stage.",
                        color = Muted, fontSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        } else {
            Text("FILTER PRESETS", color = Muted, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Original", "Cinema", "Warm", "Cool", "B&W", "Fade", "Vivid", "Soft")
                    .forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Yellow,
                                selectedLabelColor = Black
                            )
                        )
                    }
            }
        }

        Text("FILTER PRESETS", color = Muted, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
        TextButton(onClick = {
                zoom = 1f
                offsetX = 0f
                offsetY = 0f
                selectedFilter = "Original"
                exposure = 0f
                contrast = 1f
                saturation = 1f
                temperature = 0f
                highlights = 0f
                shadows = 0f
            }) {
                Text("Reset", color = Yellow, fontSize = 11.sp)
            }
    }
}

private fun wallpaperFilter(name: String): ColorMatrix {
    val matrix = ColorMatrix()
    when (name) {
        "Cinema" -> matrix.setToScale(1.18f, 1.18f, 1.18f, 1f)
        "Warm" -> matrix.setToScale(1.12f, 1.04f, 0.88f, 1f)
        "Cool" -> matrix.setToScale(0.90f, 1.04f, 1.14f, 1f)
        "B&W" -> matrix.setToSaturation(0f)
        "Fade" -> {
            matrix.setToScale(0.88f, 0.88f, 0.88f, 1f)
            matrix.values[4] = 18f
            matrix.values[9] = 18f
            matrix.values[14] = 18f
        }
        "Vivid" -> matrix.setToSaturation(1.45f)
        "Soft" -> matrix.setToScale(0.96f, 0.96f, 0.96f, 1f)
    }
    return matrix
}


@Composable
private fun AdjustmentControl(
    label: String,
    value: Float,
    valueRangeStart: Float,
    valueRangeEnd: Float,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 11.sp)
            Text(
                String.format(java.util.Locale.US, format, value),
                color = Yellow,
                fontSize = 10.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRangeStart..valueRangeEnd,
            colors = SliderDefaults.colors(
                thumbColor = Yellow,
                activeTrackColor = Yellow,
                inactiveTrackColor = SurfaceLight
            ),
            modifier = Modifier.height(30.dp)
        )
    }
}

private fun adjustedWallpaperFilter(
    name: String,
    exposure: Float,
    contrast: Float,
    saturation: Float,
    temperature: Float,
    highlights: Float,
    shadows: Float
): ColorMatrix {
    val preset = wallpaperFilter(name)
    val exp = Math.pow(2.0, exposure.toDouble()).toFloat()
    val c = contrast
    val sat = saturation
    val temp = temperature * 0.15f
    val toneScale = 1f + highlights * 0.10f
    val offset = 128f * (1f - c) + shadows * 22f - highlights * 10f

    val r = exp * toneScale * (1f + temp) * c
    val g = exp * toneScale * c
    val b = exp * toneScale * (1f - temp) * c

    val inv = 1f - sat
    val lr = 0.213f * inv
    val lg = 0.715f * inv
    val lb = 0.072f * inv

    val adjustment = ColorMatrix(floatArrayOf(
        (lr + sat) * r, lg * r, lb * r, 0f, offset,
        lr * g, (lg + sat) * g, lb * g, 0f, offset,
        lr * b, lg * b, (lb + sat) * b, 0f, offset,
        0f, 0f, 0f, 1f, 0f
    ))
    return combineColorMatrices(preset, adjustment)
}

private fun combineColorMatrices(
    first: ColorMatrix,
    second: ColorMatrix
): ColorMatrix {
    val a = first.values
    val b = second.values
    val result = FloatArray(20)

    for (row in 0 until 4) {
        for (column in 0 until 4) {
            var value = 0f
            for (k in 0 until 4) {
                value += b[row * 5 + k] * a[k * 5 + column]
            }
            result[row * 5 + column] = value
        }

        var offset = b[row * 5 + 4]
        for (k in 0 until 4) {
            offset += b[row * 5 + k] * a[k * 5 + 4]
        }
        result[row * 5 + 4] = offset
    }

    return ColorMatrix(result)
}

@Composable
private fun ToolButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = SurfaceDark,
        shape = RoundedCornerShape(13.dp)
    ) {
        Column(
            Modifier.width(75.dp).padding(vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Yellow, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 11.sp)
        }
    }
}

@Composable
private fun OverviewPage(
    onOpenEditor: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(22.dp)) {
        Text("WALLORA", color = Yellow, fontSize = 13.sp,
            fontWeight = FontWeight.Bold)
        Text("STUDIO", fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(25.dp))
        Text("Your creative workspace", fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Create, preview and prepare wallpapers for the WALLORA catalog.",
            color = Muted)
        Spacer(Modifier.height(22.dp))
        Surface(color = SurfaceDark, shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("CREATE SOMETHING", color = Yellow, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(9.dp))
                Text("Wallpaper Editor", fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)
                Text("Work with images, motion wallpapers and phone previews.",
                    color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(17.dp))
                Button(
                    onClick = onOpenEditor,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Yellow, contentColor = Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Open editor") }
                OutlinedButton(onClick = onImport,
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Import media")
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        StatusPanel("Backend not connected",
            "Local editing and previews are being built first. Cloud publishing will be connected separately.")
    }
}

@Composable
private fun CatalogPage(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(22.dp)) {
        Text("Catalog", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        StatusPanel("Catalog not connected",
            "Published wallpapers will appear here after the shared content backend is configured.")
    }
}

@Composable
private fun TeamPage(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())
        .padding(22.dp)) {
        Text("Team access", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        listOf(
            "Owner" to "Full ownership and access control",
            "Administrator" to "Manage content and publishing",
            "Editor" to "Prepare and submit wallpapers",
            "Reviewer" to "Review submitted content"
        ).forEach { (role, description) ->
            Surface(color = SurfaceDark, shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Column(Modifier.padding(15.dp)) {
                    Text(role, color = Yellow, fontWeight = FontWeight.SemiBold)
                    Text(description, color = Muted, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Role permissions must be enforced by the backend.",
            color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun PublishPage(
    title: String,
    onTitle: (String) -> Unit,
    description: String,
    onDescription: (String) -> Unit,
    category: String,
    onCategory: (String) -> Unit,
    wallpaperUri: Uri?,
    previewUri: Uri?,
    wallpaperIsVideo: Boolean,
    onSelectWallpaper: () -> Unit,
    onRemoveWallpaper: () -> Unit,
    onSelectPreview: () -> Unit,
    onRemovePreview: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back to editor", color = Yellow)
        }

        Text("Prepare for publishing", fontSize = 23.sp,
            fontWeight = FontWeight.Bold, color = Color.White)
        Text("Two independent assets. Never use the mockup as the download.",
            color = Muted, fontSize = 13.sp)

        Spacer(Modifier.height(20.dp))
        Text("1 · ORIGINAL WALLPAPER", color = Yellow, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Surface(color = SurfaceDark, shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                if (wallpaperUri != null) {
                    if (wallpaperIsVideo) {
                        Box(Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center) {
                            Text("VIDEO WALLPAPER SELECTED", color = Color.White)
                        }
                    } else {
                        AsyncImage(
                            model = wallpaperUri,
                            contentDescription = "Original wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                                .height(160.dp).clip(RoundedCornerShape(10.dp))
                        )
                    }
                    Text("Original asset selected", color = Color.White)
                } else {
                    Box(Modifier.fillMaxWidth().height(110.dp),
                        contentAlignment = Alignment.Center) {
                        Text("No wallpaper selected", color = Muted)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onSelectWallpaper,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Yellow, contentColor = Black
                        )) {
                        Text(if (wallpaperUri == null) "Choose wallpaper" else "Replace wallpaper")
                    }
                    if (wallpaperUri != null) {
                        OutlinedButton(onClick = onRemoveWallpaper) {
                            Text("Remove", color = Color.White)
                        }
                    }
                }
                Text("Actual downloadable image or video.", color = Muted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("2 · PHONE PREVIEW MOCKUP", color = Yellow, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Surface(color = SurfaceDark, shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                if (previewUri != null) {
                    AsyncImage(
                        model = previewUri,
                        contentDescription = "Phone mockup preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                            .height(210.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Text("Separate mockup selected", color = Color.White)
                } else {
                    Box(Modifier.fillMaxWidth().height(110.dp),
                        contentAlignment = Alignment.Center) {
                        Text("Optional phone mockup", color = Muted)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onSelectPreview,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Yellow, contentColor = Black
                        )) {
                        Text(if (previewUri == null) "Choose phone preview" else "Replace preview")
                    }
                    if (previewUri != null) {
                        OutlinedButton(onClick = onRemovePreview) {
                            Text("Remove", color = Color.White)
                        }
                    }
                }
                Text("Presentation only. Never used as the download.",
                    color = Muted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("WALLPAPER DETAILS", color = Yellow, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = title, onValueChange = onTitle,
            label = { Text("Wallpaper title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = description, onValueChange = onDescription,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(), minLines = 2
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = category, onValueChange = onCategory,
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))
        when {
            wallpaperUri == null ->
                StatusPanel("Wallpaper required",
                    "Choose the original wallpaper asset.")
            title.isBlank() ->
                StatusPanel("Title required",
                    "Add a title to prepare this draft.")
            else ->
                StatusPanel("Draft ready",
                    "Assets and details are selected. Server upload and publishing are not connected.")
        }

        Spacer(Modifier.height(12.dp))
        Text("Local selection only. Nothing has been uploaded or published.",
            color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun StatusPanel(title: String, detail: String) {
    Surface(color = SurfaceDark, shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Yellow, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text(detail, color = Muted, fontSize = 13.sp)
        }
    }
}

private fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}
