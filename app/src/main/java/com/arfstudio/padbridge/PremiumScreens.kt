package com.arfstudio.padbridge

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val PbBackground = Color(0xFF0A1020)
internal val PbSurface = Color(0xFF141F32)
internal val PbSurfaceElevated = Color(0xFF1A2940)
internal val PbAccent = Color(0xFF28D7C5)
internal val PbAccent2 = Color(0xFF22B8F0)
internal val PbText = Color(0xFFF3F9FF)
internal val PbMuted = Color(0xFF9CB1C8)
internal val PbStroke = Color(0xFF2B3D55)

@Composable
fun PadBridgeHeader() {
    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(43.dp).clip(RoundedCornerShape(14.dp)).background(PbAccent.copy(alpha=.13f)),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SportsEsports, null, tint = PbAccent, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("PadBridge", fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, color = PbText)
            Text("YOUR GAME. YOUR CONTROLS.", fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp, fontSize = 9.sp, color = PbAccent)
        }
        Surface(shape = RoundedCornerShape(30.dp), color = PbSurfaceElevated) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(PbAccent, RoundedCornerShape(8.dp)))
                Spacer(Modifier.width(6.dp))
                Text("LOCAL", color = PbMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DesignCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
        .background(PbSurface).border(1.dp, PbStroke.copy(alpha=.75f), RoundedCornerShape(22.dp))
        .padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
fun SectionLabel(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = PbText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, color = PbAccent, fontSize = 12.sp) }
        }
    }
}

@Composable
fun PremiumDashboard(controllers: List<ControllerInfo>, games: List<SavedGame>,
    bindingsCount: Int, navigate: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF173C4C), Color(0xFF11263E), Color(0xFF122037))))
            .border(1.dp, PbAccent.copy(alpha=.28f), RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Surface(shape = RoundedCornerShape(50), color = PbAccent.copy(alpha = .15f)) {
                Text("●  CONTROLLER STATUS", color = PbAccent, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
            }
            Icon(Icons.Default.SportsEsports, null, tint = PbAccent.copy(alpha=.85f),
                modifier = Modifier.size(48.dp))
            Text(if (controllers.isEmpty()) "Ready when you are." else "Gamepad connected.",
                fontSize = 27.sp, color = PbText, fontWeight = FontWeight.ExtraBold)
            Text(controllers.firstOrNull()?.name ?: "Connect your Bluetooth controller to begin testing inputs.",
                color = PbMuted, fontSize = 13.sp)
            Button(onClick = { navigate(2) }, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Tune, null); Spacer(Modifier.width(8.dp))
                Text("Open Controller Lab")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DesignCard(Modifier.weight(1f)) {
                Icon(Icons.Default.GridView, null, tint = PbAccent2)
                Text(games.size.toString(), fontSize = 29.sp, fontWeight = FontWeight.Bold, color = PbText)
                Text("Saved games", color = PbMuted, fontSize = 12.sp)
            }
            DesignCard(Modifier.weight(1f)) {
                Icon(Icons.Default.TouchApp, null, tint = PbAccent)
                Text(bindingsCount.toString(), fontSize = 29.sp, fontWeight = FontWeight.Bold, color = PbText)
                Text("Active targets", color = PbMuted, fontSize = 12.sp)
            }
        }
        SectionLabel("Quick actions")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DesignCard(Modifier.weight(1f).clickable { navigate(1) }) {
                Icon(Icons.Default.AddCircleOutline, null, tint = PbAccent, modifier = Modifier.size(28.dp))
                Text("Add a game", fontWeight = FontWeight.Bold, color = PbText)
                Text("Manage your library", fontSize = 11.sp, color = PbMuted)
            }
            DesignCard(Modifier.weight(1f).clickable { navigate(3) }) {
                Icon(Icons.Default.ControlPoint, null, tint = PbAccent2, modifier = Modifier.size(28.dp))
                Text("Edit controls", fontWeight = FontWeight.Bold, color = PbText)
                Text("Set touch positions", fontSize = 11.sp, color = PbMuted)
            }
        }
        SectionLabel("Setup & activation", "View guide") { navigate(5) }
        DesignCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.InfoOutline, null, tint = PbAccent2)
                Spacer(Modifier.width(10.dp))
                Text("Mapping engine not activated", color = PbText, fontWeight = FontWeight.SemiBold)
            }
            Text("You can configure and save layouts. Sending touch gestures into games is not available yet.",
                color = PbMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(5.dp))
    }
}

@Composable
fun PremiumGames(games: List<SavedGame>, installed: () -> List<LaunchableApp>,
    add: (LaunchableApp) -> Unit, remove: (String) -> Unit, launch: (String) -> Unit,
    configure: (String) -> Unit) {
    var picker by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<SavedGame?>(null) }
    val available = remember(picker) { if (picker) installed() else emptyList() }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Game library", color = PbText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Your games, ready to configure", color = PbMuted, fontSize = 12.sp)
            }
            FilledIconButton(onClick = { picker = true }) { Icon(Icons.Default.Add, "Add game") }
        }
        if (games.isEmpty()) DesignCard {
            Icon(Icons.Default.SportsEsports, null, tint = PbAccent, modifier = Modifier.size(40.dp))
            Text("Your library is empty", color = PbText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            Text("Add any installed game to begin building your custom touch layout.", color = PbMuted)
            Button(onClick = { picker = true }) { Text("Add first game") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(games, key = { it.packageName }) { game ->
                DesignCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).background(PbSurfaceElevated, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VideogameAsset, null, tint = PbAccent2)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(game.label, color = PbText, fontWeight = FontWeight.Bold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(game.packageName, color = PbMuted, fontSize = 10.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { deleting = game }) { Icon(Icons.Default.DeleteOutline, "Remove game", tint = PbMuted) }
                    }
                    Text("PROFILE  ·  " + game.profileName, color = PbAccent, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { configure(game.packageName) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Tune, null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(5.dp)); Text("Configure")
                        }
                        OutlinedButton(onClick = { launch(game.packageName) }) { Text("Launch") }
                    }
                }
            }
        }
    }
    if (picker) AlertDialog(onDismissRequest = { picker = false }, title = { Text("Add installed game") },
        text = {
            Column {
                OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                    label = { Text("Search apps") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(available.filter { it.label.contains(query, true) || it.packageName.contains(query,true) }) { app ->
                        Row(Modifier.fillMaxWidth().clickable { add(app); picker = false; query = "" }
                            .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddCircleOutline, null, tint = PbAccent)
                            Spacer(Modifier.width(10.dp))
                            Text(app.label, color = PbText, maxLines = 1)
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { picker = false }) { Text("Close") } })
    deleting?.let { game ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Remove game?") },
            text = { Text("Remove " + game.label + " from your library? Its existing touch bindings will remain stored.") },
            confirmButton = { TextButton(onClick = { remove(game.packageName); deleting = null }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } })
    }
}

@Composable
fun PremiumProfiles(games: List<SavedGame>, rename: (String, String) -> Unit, edit: (String) -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Profiles", color = PbText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Customize each game's saved layout name", color = PbMuted, fontSize = 12.sp)
        if (games.isEmpty()) DesignCard {
            Text("No profiles yet", color = PbText, fontWeight = FontWeight.Bold)
            Text("Add a game first to create its default profile.", color = PbMuted)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(games, key = { it.packageName }) { game ->
                var edited by remember(game.packageName, game.profileName) { mutableStateOf(game.profileName) }
                DesignCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, null, tint = PbAccent)
                        Spacer(Modifier.width(9.dp))
                        Text(game.label, color = PbText, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(value = edited, onValueChange = { edited = it.take(60) },
                        label = { Text("Profile name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { rename(game.packageName, edited.trim()) },
                            enabled = edited.isNotBlank() && edited.trim() != game.profileName) { Text("Save") }
                        OutlinedButton(onClick = { edit(game.packageName) }) { Text("Edit mappings") }
                    }
                    Text("One layout per game in this build.", color = PbMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PremiumSetup(goController: () -> Unit, goMapping: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text("Setup center", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = PbText)
        Text("A straightforward path from pairing to playable mappings", color = PbMuted, fontSize = 13.sp)
        SetupStep("01", "Connect your gamepad", "Pair your controller in Android Bluetooth settings. Then open Controller Lab to verify each input.", true, goController)
        SetupStep("02", "Calibrate controller", "Move both sticks and press the triggers. Save dead-zone and inversion preferences for later mapping support.", true, goController)
        SetupStep("03", "Build a touch layout", "Choose a saved game, add controller buttons and drag their targets onto the layout canvas.", true, goMapping)
        SetupStep("04", "Enable in-game mapping", "Pending Android 16 testing: overlay, accessibility gesture dispatch and background controller input are not implemented.", false, null)
        DesignCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null, tint = PbAccent)
                Spacer(Modifier.width(8.dp))
                Text("Private by design", color = PbText, fontWeight = FontWeight.Bold)
            }
            Text("Profiles and calibration settings are stored locally. No account or cloud service is needed.",
                color = PbMuted, fontSize = 12.sp)
        }
    }
}
@Composable
private fun SetupStep(number: String, title: String, description: String,
    available: Boolean, action: (() -> Unit)?) {
    DesignCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(37.dp).background(PbAccent.copy(alpha=.13f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center) {
                Text(number, color = PbAccent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Text(title, color = PbText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(if (available) "READY" else "PLANNED",
                color = if (available) PbAccent else PbMuted, fontSize = 10.sp)
        }
        Text(description, color = PbMuted, fontSize = 12.sp)
        if (action != null) OutlinedButton(onClick = action) { Text("Open") }
    }
}
