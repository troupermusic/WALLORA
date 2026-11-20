package com.kifs.wallora

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.walloraStore by preferencesDataStore("wallora_native_preferences")
private val FAVORITES_KEY = stringSetPreferencesKey("favorites")
private val RECENTS_KEY = stringPreferencesKey("recent_order")
private val DOWNLOADS_KEY = stringSetPreferencesKey("downloads")
private val YELLOW = Color(0xFFFFD900)
private val BLACK = Color(0xFF050505)
private val SURFACE = Color(0xFF171717)
private val MUTED = Color(0xFF858585)


private data class Wallpaper(
    val id: Int,
    val title: String,
    val category: String,
    val tag: String,
    val image: String,
    val collection: String
)


private val catalog = listOf(
    Wallpaper(1, "Midnight Peaks", "Nature", "TRENDING", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=90", "Nature Escape"),
    Wallpaper(2, "Neon Nights", "Abstract", "POPULAR", "https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=1200&q=90", "Dark & Minimal"),
    Wallpaper(3, "Silent Ocean", "Nature", "NEW", "https://images.unsplash.com/photo-1518837695005-2083093ee35b?auto=format&fit=crop&w=1200&q=90", "Nature Escape"),
    Wallpaper(4, "Dark Motion", "AMOLED", "4K", "https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?auto=format&fit=crop&w=1200&q=90", "Dark & Minimal"),
    Wallpaper(5, "Lost in Space", "Space", "FEATURED", "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=1200&q=90", "Beyond Earth"),
    Wallpaper(6, "Golden Horizon", "Nature", "NEW", "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=1200&q=90", "Nature Escape"),
    Wallpaper(7, "Concrete Dreams", "Architecture", "EDITOR PICK", "https://images.unsplash.com/photo-1511818966892-d7d671e672a2?auto=format&fit=crop&w=1200&q=90", "Architecture"),
    Wallpaper(8, "Red Planet", "Space", "4K", "https://images.unsplash.com/photo-1614728263952-84ea256f9679?auto=format&fit=crop&w=1200&q=90", "Beyond Earth")
)
private val categories = listOf("All", "AMOLED", "Nature", "Abstract", "Space", "Architecture")
private val collections = listOf("All", "Dark & Minimal", "Nature Escape", "Beyond Earth", "Architecture")


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        setContent { WalloraTheme { WalloraLaunchHost() } }
    }
}




@Composable
private fun WalloraLaunchHost() {
    val context = LocalContext.current
    var launchComplete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()

        // Prepare persistent app preferences before revealing the main screen.
        withContext(Dispatchers.IO) {
            context.walloraStore.data.first()
        }

        // Allow enough time for the entrance animation to be visible,
        // without imposing a long artificial loading delay.
        val remaining = 850L - (System.currentTimeMillis() - start)
        if (remaining > 0) delay(remaining)

        launchComplete = true
    }

    Box(Modifier.fillMaxSize()) {
        WalloraApp()

        AnimatedVisibility(
            visible = !launchComplete,
            modifier = Modifier.fillMaxSize(),
            exit = fadeOut(tween(450)) +
                scaleOut(targetScale = 1.015f, animationSpec = tween(450))
        ) {
            WalloraSplash()
        }
    }
}

@Composable
private fun WalloraSplash() {
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        entered = true
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(650),
        label = "walloraLogoAlpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.90f,
        animationSpec = tween(700),
        label = "walloraLogoScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BLACK),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .border(1.5.dp, YELLOW, RoundedCornerShape(17.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "W",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Text(
                    "WALLORA",
                    modifier = Modifier.padding(top = 19.dp),
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )

                Text(
                    "YOUR SCREEN. YOUR STYLE.",
                    modifier = Modifier.padding(top = 8.dp),
                    color = YELLOW,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    "Beautiful wallpapers. A different kind of screen.",
                    modifier = Modifier.padding(top = 10.dp),
                    color = MUTED,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(55.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(25.dp),
                color = YELLOW,
                trackColor = Color(0xFF202020),
                strokeWidth = 2.dp
            )

            Text(
                "INITIALIZING WALLORA",
                modifier = Modifier.padding(top = 17.dp),
                color = Color(0xFF777777),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
    }
}


@Composable
private fun WalloraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = YELLOW,
            background = BLACK,
            surface = SURFACE,
            onPrimary = BLACK,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}


@Composable
private fun WalloraApp() {
    val context = LocalContext.current
    val prefs by context.walloraStore.data.collectAsState(initial = androidx.datastore.preferences.core.emptyPreferences())
    val favorites = prefs[FAVORITES_KEY] ?: emptySet()
    val recentOrder = (prefs[RECENTS_KEY] ?: "").split(",").filter { it.isNotBlank() }
    val recentIds = recentOrder.toSet()
    val downloads = prefs[DOWNLOADS_KEY] ?: emptySet()
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf("Home") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedCollection by remember { mutableStateOf("All") }
    var mode by remember { mutableStateOf("Discover") }
    var search by remember { mutableStateOf("") }
    var preview by remember { mutableStateOf<Wallpaper?>(null) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var clockTick by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            clockTick = System.currentTimeMillis()
        }
    }

    fun recordViewed(wallpaper: Wallpaper) {
        val id = wallpaper.id.toString()
        scope.launch {
            context.walloraStore.edit { data ->
                val old = (data[RECENTS_KEY] ?: "").split(",").filter { it.isNotBlank() }
                data[RECENTS_KEY] = (listOf(id) + old.filterNot { it == id }).take(50).joinToString(",")
            }
        }
    }

    fun toggleSaved(wallpaper: Wallpaper) {
        val id = wallpaper.id.toString()
        scope.launch {
            context.walloraStore.edit { data ->
                data[FAVORITES_KEY] = (data[FAVORITES_KEY] ?: emptySet()).toMutableSet().apply {
                    if (contains(id)) remove(id) else add(id)
                }
            }
        }
    }

    val filteredWallpapers = remember(activeTab, selectedCategory, selectedCollection, mode, search, favorites, recentIds, downloads, recentOrder) {
        var list = catalog
        when (activeTab) {
            "Saved" -> list = list.filter { favorites.contains(it.id.toString()) }
            "Downloads" -> list = list.filter { downloads.contains(it.id.toString()) }
            "Recent" -> list = list.filter { recentIds.contains(it.id.toString()) }.sortedBy { recentOrder.indexOf(it.id.toString()) }
            "Explore" -> if (selectedCategory != "All") list = list.filter { it.category == selectedCategory }
        }
        if (activeTab == "Home" && mode == "Trending") list = list.filter { it.tag in listOf("TRENDING", "POPULAR", "FEATURED", "EDITOR PICK") }
        if (activeTab == "Home" && mode == "Fresh Drops") list = list.filter { it.tag == "NEW" }
        if (selectedCategory != "All" && activeTab == "Home") list = list.filter { it.category == selectedCategory }
        if (selectedCollection != "All") list = list.filter { it.collection == selectedCollection }
        if (search.isNotBlank()) list = list.filter { (it.title + " " + it.category + " " + it.collection).contains(search, ignoreCase = true) }
        list
    }

    Scaffold(
        containerColor = BLACK,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF080808), tonalElevation = 0.dp) {
                val destinations = listOf("Home", "Explore", "Saved", "Downloads")
                destinations.forEach { destination ->
                    val icon = when (destination) {
                        "Home" -> Icons.Default.Home
                        "Explore" -> Icons.Default.Explore
                        "Saved" -> Icons.Default.Bookmark
                        else -> Icons.Default.Download
                    }
                    NavigationBarItem(
                        selected = activeTab == destination,
                        onClick = {
                            activeTab = destination
                            if (destination == "Home") selectedCategory = "All"
                        },
                        icon = { Icon(icon, contentDescription = destination) },
                        label = { Text(destination, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = YELLOW,
                            selectedTextColor = YELLOW,
                            indicatorColor = Color(0xFF242000),
                            unselectedIconColor = MUTED,
                            unselectedTextColor = MUTED
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 17.dp).background(BLACK)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(62.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(Modifier.size(27.dp).border(1.dp, Color.White), contentAlignment = Alignment.Center) {
                        Text("W", fontWeight = FontWeight.ExtraBold)
                    }
                    Text("WALLORA", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.5.sp)
                    Text(".", color = YELLOW, fontWeight = FontWeight.Black)
                }
                IconButton(onClick = { activeTab = "Saved" }) {
                    Icon(Icons.Default.FavoriteBorder, "Favorites", tint = YELLOW)
                }
            }

            Column(Modifier.weight(1f)) {
                Column(Modifier.padding(top = 24.dp, bottom = 20.dp)) {
                    Text("✦  YOUR SCREEN. YOUR STYLE.", color = YELLOW, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
                    Text("Find your\nwall.", fontSize = 48.sp, lineHeight = 47.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp, modifier = Modifier.padding(top = 10.dp))
                    Text("Beautiful wallpapers. A different kind of screen.", color = MUTED, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
                }

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    placeholder = { Text("Find your next wallpaper...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MUTED) },
                    trailingIcon = { if (search.isNotEmpty()) IconButton(onClick = { search = "" }) { Icon(Icons.Default.Close, null) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF303030), unfocusedBorderColor = Color(0xFF252525),
                        focusedContainerColor = Color(0xFF111111), unfocusedContainerColor = Color(0xFF111111), cursorColor = YELLOW
                    )
                )

                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 13.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Discover", "Trending", "Fresh Drops").forEach { filterMode ->
                        FilterChip(
                            selected = mode == filterMode,
                            onClick = { mode = filterMode; selectedCollection = "All"; activeTab = "Home"; selectedCategory = "All" },
                            label = { Text(filterMode, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF171700), selectedLabelColor = YELLOW),
                            border = FilterChipDefaults.filterChipBorder(selected = mode == filterMode, enabled = true, selectedBorderColor = YELLOW, borderColor = Color(0xFF262626))
                        )
                    }
                    TextButton(onClick = { activeTab = "Recent" }) {
                        Icon(Icons.Default.AccessTime, null, tint = YELLOW, modifier = Modifier.size(16.dp))
                        Text("Recent", color = YELLOW, fontSize = 11.sp)
                    }
                }

                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat; if (activeTab == "Saved" || activeTab == "Downloads") activeTab = "Explore" },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = YELLOW, selectedLabelColor = BLACK, labelColor = MUTED),
                            border = FilterChipDefaults.filterChipBorder(selected = selectedCategory == cat, enabled = true, selectedBorderColor = YELLOW, borderColor = Color(0xFF242424))
                        )
                    }
                }

                Text("COLLECTIONS", color = MUTED, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 9.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    collections.forEach { coll ->
                        FilterChip(
                            selected = selectedCollection == coll,
                            onClick = { selectedCollection = coll; mode = "Discover"; activeTab = "Home"; selectedCategory = "All" },
                            label = { Text(coll, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF171700), selectedLabelColor = YELLOW),
                            border = FilterChipDefaults.filterChipBorder(selected = selectedCollection == coll, enabled = true, selectedBorderColor = YELLOW, borderColor = Color(0xFF242424))
                        )
                    }
                }

                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(when (activeTab) { "Saved" -> "YOUR COLLECTION"; "Downloads" -> "OFFLINE COLLECTION"; "Recent" -> "RECENTLY VIEWED"; else -> "CURATED FOR YOU" }, color = MUTED, fontSize = 9.sp, letterSpacing = 1.4.sp)
                        Text(when (activeTab) { "Saved" -> "Saved wallpapers"; "Downloads" -> "Your downloads"; "Recent" -> "Recently viewed"; "Explore" -> "Explore"; else -> "Discover beauty" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${filteredWallpapers.size} WALLPAPERS", color = Color.Gray, fontSize = 9.sp)
                }

                if (filteredWallpapers.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nothing here yet\nExplore wallpapers and save your favorites.", color = MUTED)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredWallpapers, key = { it.id }) { wallpaper ->
                            WallpaperCard(
                                wallpaper = wallpaper,
                                liked = favorites.contains(wallpaper.id.toString()),
                                onOpen = { preview = wallpaper; recordViewed(wallpaper) },
                                onFavorite = { toggleSaved(wallpaper) }
                            )
                        }
                    }
                }
            }
        }
    }


    preview?.let { selected ->
        WallpaperPreviewDialog(
            wallpaper = selected,
            liked = favorites.contains(selected.id.toString()),
            onClose = { preview = null },
            onFavorite = { toggleSaved(selected) },
            onDownload = {
                scope.launch {
                    downloadWallpaper(context, selected)
                    context.walloraStore.edit { data -> data[DOWNLOADS_KEY] = (data[DOWNLOADS_KEY] ?: emptySet()) + selected.id.toString() }
                }
            },
            now = clockTick,
            onApply = { showApplyDialog = true }
        )
    }

    if (showApplyDialog && preview != null) {
        ApplyWallpaperDialog(
            wallpaper = preview!!,
            onDismiss = { showApplyDialog = false },
            onApply = { flags ->
                applyWallpaper(context, preview!!, flags) { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
                showApplyDialog = false
            }
        )
    }
}


@Composable
private fun WallpaperCard(wallpaper: Wallpaper, liked: Boolean, onOpen: () -> Unit, onFavorite: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.975f else 1f, label = "wallpaperCardScale")

    Column(Modifier.scale(scale).animateContentSize()) {
        Box(Modifier.fillMaxWidth().height(205.dp).clip(RoundedCornerShape(12.dp)).background(SURFACE)) {
            AsyncImage(
                model = wallpaper.image,
                contentDescription = wallpaper.title,
                modifier = Modifier.fillMaxSize().clickable(onClick = onOpen),
                contentScale = ContentScale.Crop
            )
            Text(wallpaper.tag, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).padding(9.dp).background(Color.Black.copy(alpha = .65f), RoundedCornerShape(5.dp)).padding(horizontal = 7.dp, vertical = 5.dp))
            Text("›", fontSize = 20.sp, color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(27.dp).background(Color.Black.copy(alpha = .6f), CircleShape).wrapContentSize())
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(wallpaper.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(wallpaper.category, fontSize = 10.sp, color = MUTED)
            }
            IconButton(onClick = onFavorite, modifier = Modifier.size(35.dp), interactionSource = interaction) {
                Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorite", tint = if (liked) YELLOW else MUTED)
            }
        }
    }
}


@Composable
private fun WallpaperPreviewDialog(wallpaper: Wallpaper, liked: Boolean, onClose: () -> Unit, onFavorite: () -> Unit, onDownload: () -> Unit, now: Long, onApply: () -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn(initialScale = .96f), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(BLACK)) {
                AsyncImage(wallpaper.image, wallpaper.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .24f))) {}
                Column(Modifier.align(Alignment.TopCenter).padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now)), fontSize = 54.sp, fontWeight = FontWeight.Light)
                    Text(SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(now)), fontSize = 16.sp)
                }
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).padding(top = 25.dp, start = 12.dp).background(Color.Black.copy(alpha = .5f), CircleShape)) {
                    Icon(Icons.Default.Close, "Close")
                }
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp)) {
                    Text(wallpaper.category.uppercase(), color = YELLOW, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                    Text(wallpaper.title, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp, bottom = 16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onFavorite, modifier = Modifier.size(52.dp).background(SURFACE, RoundedCornerShape(14.dp))) {
                            Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (liked) YELLOW else Color.White)
                        }
                        Button(onClick = onDownload, modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = YELLOW, contentColor = BLACK), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Default.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download wallpaper", fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Wallpaper, null, tint = YELLOW)
                        Spacer(Modifier.width(7.dp))
                        Text("Set as wallpaper", color = YELLOW)
                    }
                }
            }
        }
    }
}


@Composable
private fun ApplyWallpaperDialog(wallpaper: Wallpaper, onDismiss: () -> Unit, onApply: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SURFACE,
        title = { Text("Set wallpaper") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose where to apply ${wallpaper.title}", color = MUTED)
                listOf(
                    "Home screen" to WallpaperManager.FLAG_SYSTEM,
                    "Lock screen" to WallpaperManager.FLAG_LOCK,
                    "Both screens" to (WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                ).forEach { (label, flag) ->
                    Button(onClick = { onApply(flag) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = YELLOW, contentColor = BLACK)) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MUTED) } }
    )
}


private fun downloadWallpaper(context: Context, wallpaper: Wallpaper) {
    try {
        val request = DownloadManager.Request(Uri.parse(wallpaper.image))
            .setTitle("WALLORA • ${wallpaper.title}")
            .setDescription("Downloading wallpaper")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "WALLORA/${wallpaper.title.replace(' ', '-')}.jpg")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        context.getSystemService(DownloadManager::class.java).enqueue(request)
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
    } catch (error: Exception) {
        Toast.makeText(context, "Download failed: ${error.message}", Toast.LENGTH_LONG).show()
    }
}


private fun applyWallpaper(context: Context, wallpaper: Wallpaper, flags: Int, report: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            URL(wallpaper.image).openStream().use { stream ->
                WallpaperManager.getInstance(context).setStream(stream, null, true, flags)
            }
            withContext(Dispatchers.Main) { report("Wallpaper applied") }
        } catch (error: Exception) {
            withContext(Dispatchers.Main) { report("Could not set wallpaper: ${error.message}") }
        }
    }
}
