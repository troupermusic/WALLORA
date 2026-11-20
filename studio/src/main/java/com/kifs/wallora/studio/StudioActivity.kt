package com.kifs.wallora.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Black = Color(0xFF050505)
private val SurfaceDark = Color(0xFF151515)
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
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var wallpaperTitle by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImage = uri?.toString()
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
            bottomBar = {
                NavigationBar(containerColor = SurfaceDark) {
                    listOf("Overview", "Publish", "Catalog", "Team").forEach { item ->
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            icon = {},
                            label = { Text(item, fontSize = 11.sp) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Text("WALLORA", color = Yellow, fontSize = 13.sp)
                Text("STUDIO", color = Color.White, fontSize = 28.sp)
                Spacer(Modifier.height(22.dp))

                when (page) {
                    "Overview" -> {
                        Text("Your content workspace", fontSize = 20.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Manage wallpapers for the WALLORA app from one place.",
                            color = Muted
                        )
                        Spacer(Modifier.height(24.dp))
                        StatusPanel(
                            "Backend not connected",
                            "Uploads and publishing are not live yet. Your public WALLORA app is unaffected."
                        )
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = { page = "Publish" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Yellow,
                                contentColor = Black
                            )
                        ) {
                            Text("Prepare a wallpaper")
                        }
                    }

                    "Publish" -> {
                        Text("New wallpaper", fontSize = 21.sp)
                        Spacer(Modifier.height(18.dp))
                        OutlinedTextField(
                            value = wallpaperTitle,
                            onValueChange = { wallpaperTitle = it },
                            label = { Text("Wallpaper title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedImage == null) "Choose image" else "Choose another image")
                        }
                        if (selectedImage != null) {
                            Spacer(Modifier.height(10.dp))
                            Text("Image selected on this device", color = Yellow)
                        }
                        Spacer(Modifier.height(14.dp))
                        StatusPanel(
                            "Draft only",
                            "This selection stays on this screen. It has not been uploaded or published."
                        )
                    }

                    "Catalog" -> {
                        Text("Wallpaper catalog", fontSize = 21.sp)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Your connected wallpaper catalog will appear here when the content backend is configured.",
                            color = Muted
                        )
                    }

                    "Team" -> {
                        Text("Team access", fontSize = 21.sp)
                        Spacer(Modifier.height(14.dp))
                        listOf(
                            "Owner" to "Full ownership and access control",
                            "Administrator" to "Manage content and publishing",
                            "Editor" to "Prepare and submit wallpapers",
                            "Reviewer" to "Review submitted content"
                        ).forEach { (role, description) ->
                            Surface(
                                color = SurfaceDark,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Column(Modifier.padding(15.dp)) {
                                    Text(role, color = Yellow)
                                    Text(description, color = Muted, fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Role permissions will be enforced by the backend, not just this interface.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPanel(title: String, detail: String) {
    Surface(
        color = SurfaceDark,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Yellow, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text(detail, color = Muted, fontSize = 13.sp)
        }
    }
}
