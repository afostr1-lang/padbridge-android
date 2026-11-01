package com.arfstudio.padbridge

import android.content.Intent
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val background = PbBackground
private val panel = PbSurface
private val accent = PbAccent
private val muted = PbMuted
private val white = PbText
data class LaunchableApp(val packageName: String, val label: String)

class MainActivity : ComponentActivity() {
    private lateinit var monitor: ControllerMonitor
    private lateinit var store: GameStore
    private lateinit var mappingStore: MappingStore
    private lateinit var calibrationStore: CalibrationStore
    private val bindings = mutableStateListOf<TouchBinding>()
    private val axes = mutableStateMapOf<Int, Float>()
    private var selectedGame by mutableStateOf<String?>(null)
    private val controllers = mutableStateListOf<ControllerInfo>()
    private val games = mutableStateListOf<SavedGame>()
    private var diagnostic by mutableStateOf("Press a button or move an analog stick.")
    private var selectedPage by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = GameStore(applicationContext)
        mappingStore = MappingStore(applicationContext)
        calibrationStore = CalibrationStore(applicationContext)
        monitor = ControllerMonitor(applicationContext) { runOnUiThread { refreshControllers() } }
        refreshControllers()
        refreshGames()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = PbAccent, secondary = PbAccent2,
                onPrimary = PbBackground, background = PbBackground, surface = PbSurface,
                onSurface = PbText, surfaceVariant = PbSurfaceElevated)) {
                Scaffold(containerColor = PbBackground,
                    bottomBar = {
                        NavigationBar(containerColor = PbSurface, tonalElevation = 0.dp) {
                            listOf("Home","Games","Lab","Map","Profiles","Setup").forEachIndexed { index, title ->
                                val icon = when(index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.SportsEsports
                                    2 -> Icons.Default.Tune
                                    3 -> Icons.Default.TouchApp
                                    4 -> Icons.Default.Layers
                                    else -> Icons.Default.Settings
                                }
                                NavigationBarItem(
                                    selected = selectedPage == index,
                                    onClick = { selectedPage = index },
                                    icon = { Icon(icon, title) },
                                    label = { Text(title, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PbBackground,
                                        selectedTextColor = PbAccent,
                                        indicatorColor = PbAccent,
                                        unselectedIconColor = PbMuted,
                                        unselectedTextColor = PbMuted)
                                )
                            }
                        }
                    }) { inner ->
                    Column(Modifier.fillMaxSize().padding(inner).padding(horizontal = 16.dp)) {
                        PadBridgeHeader()
                        Spacer(Modifier.height(6.dp))
                        when (selectedPage) {
                            0 -> PremiumDashboard(controllers, games, bindings.size) { selectedPage = it }
                            1 -> PremiumGames(games, ::installedApps, ::addGame, ::removeGame,
                                ::launchGame) { chooseGame(it); selectedPage = 3 }
                            2 -> CalibrationLab(controllers, axes, diagnostic,
                                calibrationStore::get, calibrationStore::save)
                            3 -> MappingStudio(games, bindings, selectedGame, ::chooseGame,
                                ::addBinding, ::moveBinding, ::removeBinding)
                            4 -> PremiumProfiles(games, ::renameProfile) {
                                chooseGame(it); selectedPage = 3
                            }
                            5 -> PremiumSetup({ selectedPage = 2 }, { selectedPage = 3 })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() { super.onResume(); monitor.start(); refreshControllers(); refreshGames() }
    override fun onPause() { monitor.stop(); super.onPause() }
    override fun onDestroy() { mappingStore.close(); store.close(); super.onDestroy() }

    private fun refreshControllers() { controllers.clear(); controllers.addAll(monitor.connected()) }
    private fun refreshGames() {
        games.clear(); games.addAll(store.all())
        if (selectedGame == null || games.none { it.packageName == selectedGame }) {
            selectedGame = games.firstOrNull()?.packageName
        }
        refreshBindings()
    }
    private fun chooseGame(game: String) { selectedGame = game; refreshBindings() }
    private fun refreshBindings() { bindings.clear(); selectedGame?.let { bindings.addAll(mappingStore.list(it)) } }
    private fun addBinding(game: String, button: String) { mappingStore.add(game,button); refreshBindings() }
    private fun moveBinding(id: Long, x: Float, y: Float) { mappingStore.move(id,x,y); refreshBindings() }
    private fun removeBinding(id: Long) { mappingStore.delete(id); refreshBindings() }
    private fun addGame(app: LaunchableApp) { store.add(app.packageName, app.label); refreshGames() }
    private fun removeGame(packageName: String) { store.remove(packageName); refreshGames() }
    private fun renameProfile(packageName: String, value: String) {
        if (value.isNotBlank()) { store.renameProfile(packageName, value.trim().take(60)); refreshGames() }
    }
    private fun launchGame(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) startActivity(intent)
        else diagnostic = "This application cannot be launched."
    }
    private fun installedApps(): List<LaunchableApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        return packageManager.queryIntentActivities(intent, 0)
            .map { LaunchableApp(it.activityInfo.packageName, it.loadLabel(packageManager).toString()) }
            .filterNot { it.packageName == packageName }
            .distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (isControllerKey(event)) {
            diagnostic = "${KeyEvent.keyCodeToString(event.keyCode)}  •  ${if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"}  •  device ${event.deviceId}"
            if (selectedPage == 2) return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK &&
            event.action == MotionEvent.ACTION_MOVE) {
            event.device?.motionRanges?.filter { it.source and InputDevice.SOURCE_CLASS_JOYSTICK != 0 }
                ?.forEach { axes[it.axis] = event.getAxisValue(it.axis) }
            diagnostic = joystickSummary(event)
            if (selectedPage == 2) return true
        }
        return super.dispatchGenericMotionEvent(event)
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)
        .background(panel, RoundedCornerShape(16.dp)).padding(16.dp), content = content)
}
@Composable
private fun Heading(title: String, subtitle: String) {
    Text(title, color = white, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
    Text(subtitle, color = muted, fontSize = 13.sp)
    Spacer(Modifier.height(16.dp))
}
@Composable
private fun HomeScreen(controllers: List<ControllerInfo>, games: List<SavedGame>, navigate: (Int) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Heading("Your dashboard", "Your games and connected input devices")
        Panel {
            Text("CONTROLLER STATUS", color = muted, fontSize = 11.sp)
            Text(if (controllers.isEmpty()) "No controller detected" else "${controllers.size} controller(s) connected",
                color = accent, fontSize = 20.sp)
            controllers.forEach { Text(it.name, color = white) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { navigate(2) }) { Text("Open diagnostics") }
        }
        Panel {
            Text("YOUR LIBRARY", color = muted, fontSize = 11.sp)
            Text("${games.size} saved game(s)", color = white, fontSize = 20.sp)
            Spacer(Modifier.height(10.dp))
            Button(onClick = { navigate(1) }) { Text("Manage games") }
        }
        Panel {
            Text("WHAT\u0027S NEW", color = accent)
            Text("Controller calibration and touch layout editor are available.", color = white)
            Text("Mappings save locally. In-game touch injection is still under development.", color = muted, fontSize = 12.sp)
        }
    }
}
@Composable
private fun ControllerScreen(controllers: List<ControllerInfo>, diagnostic: String, refresh: () -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Heading("Controller diagnostics", "Pair your gamepad in Android Bluetooth settings")
        OutlinedButton(onClick = refresh) { Text("Refresh devices") }
        Spacer(Modifier.height(14.dp))
        if (controllers.isEmpty()) Panel { Text("No compatible gamepad found.", color = white) }
        controllers.forEach { pad ->
            Panel {
                Text(pad.name, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Device ID: ${pad.id}", color = muted)
                Text("Motion ranges: ${pad.axes}", color = muted)
                Text("Descriptor: ${pad.descriptor.take(36)}", color = muted, fontSize = 11.sp)
            }
        }
        Panel {
            Text("LIVE INPUT", color = accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(diagnostic, color = white, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text("Events are visible only while PadBridge is the foreground app.", color = muted, fontSize = 12.sp)
        }
    }
}
@Composable
private fun GameScreen(games: List<SavedGame>, installed: () -> List<LaunchableApp>,
    add: (LaunchableApp) -> Unit, remove: (String) -> Unit, launch: (String) -> Unit) {
    var picker by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val apps = remember(picker) { if (picker) installed() else emptyList() }
    Column {
        Heading("Game library", "Add games from installed Android apps")
        Button(onClick = { picker = true }) { Text("+ Add game") }
        Spacer(Modifier.height(12.dp))
        if (games.isEmpty()) Text("No games added yet.", color = muted)
        LazyColumn {
            items(games, key = { it.packageName }) { game ->
                Panel {
                    Text(game.label, color = white, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(game.packageName, color = muted, fontSize = 11.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("Profile: ${game.profileName}", color = accent, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { launch(game.packageName) }) { Text("Launch") }
                        OutlinedButton(onClick = { remove(game.packageName) }) { Text("Remove") }
                    }
                }
            }
        }
    }
    if (picker) AlertDialog(onDismissRequest = { picker = false },
        title = { Text("Choose an installed game") },
        text = {
            Column {
                OutlinedTextField(value = search, onValueChange = { search = it },
                    label = { Text("Search apps") }, singleLine = true)
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(apps.filter {
                        it.label.contains(search, true) || it.packageName.contains(search, true)
                    }) { app ->
                        Text(app.label, Modifier.fillMaxWidth().clickable {
                            add(app); picker = false; search = ""
                        }.padding(12.dp), color = white)
                    }
                }
            }
        }, confirmButton = {
            TextButton(onClick = { picker = false }) { Text("Close") }
        })
}
@Composable
private fun ProfileScreen(games: List<SavedGame>, rename: (String, String) -> Unit) {
    Column {
        Heading("Saved profiles", "One named profile per game in Phase 1")
        if (games.isEmpty()) Text("Add a game to create its default profile.", color = muted)
        LazyColumn {
            items(games, key = { it.packageName }) { game ->
                var edited by remember(game.packageName, game.profileName) {
                    mutableStateOf(game.profileName)
                }
                Panel {
                    Text(game.label, color = white, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = edited, onValueChange = { edited = it.take(60) },
                        label = { Text("Profile name") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Button(enabled = edited.trim().isNotEmpty() && edited.trim() != game.profileName,
                        onClick = { rename(game.packageName, edited) }) { Text("Save name") }
                    Text("Mapping layouts arrive in Phase 2.", color = muted, fontSize = 11.sp)
                }
            }
        }
    }
}
