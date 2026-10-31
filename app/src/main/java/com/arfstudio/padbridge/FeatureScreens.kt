package com.arfstudio.padbridge

import android.view.MotionEvent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val surface = Color(0xFF172232)
private val outline = Color(0xFF304457)
private val teal = Color(0xFF55DBC7)
private val soft = Color(0xFFB5C5D7)
private val paper = Color(0xFFF4F8FC)
private val buttons = listOf("A","B","X","Y","L1","R1","L2","R2","D-Pad Up","D-Pad Down","D-Pad Left","D-Pad Right","L3","R3","Start","Select")

@Composable
private fun LabCard(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(surface, RoundedCornerShape(18.dp))
        .border(1.dp, outline, RoundedCornerShape(18.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
fun CalibrationLab(controllers: List<ControllerInfo>, axes: Map<Int, Float>, latestEvent: String,
    read: (String) -> Calibration, save: (String, Calibration) -> Unit) {
    val current = controllers.firstOrNull()
    var deadZone by remember(current?.descriptor) {
        mutableFloatStateOf(current?.let { read(it.descriptor).deadZone } ?: .12f)
    }
    var invert by remember(current?.descriptor) {
        mutableStateOf(current?.let { read(it.descriptor).invertY } ?: false)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Controller Lab", fontSize = 25.sp, color = paper, fontWeight = FontWeight.Bold)
        Text("Real-time diagnostics and calibration", color = soft)
        LabCard {
            Text(if (current == null) "No controller connected" else "● " + current.name,
                color = teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Pair the controller in Android Bluetooth settings, then return here.", color = soft, fontSize = 13.sp)
        }
        LabCard {
            Text("LIVE INPUT", color = teal, fontWeight = FontWeight.Bold)
            Text(latestEvent, color = paper, fontSize = 14.sp)
            axes.toSortedMap().forEach { (axis, value) ->
                Text(MotionEvent.axisToString(axis) + "  " + String.format(java.util.Locale.US,"%.2f",value),
                    color = soft, fontSize = 12.sp)
                LinearProgressIndicator(progress = { ((value + 1f) / 2f).coerceIn(0f,1f) },
                    modifier = Modifier.fillMaxWidth(), color = teal)
            }
            Text("Input detection operates only while PadBridge is the foreground app.",
                color = soft, fontSize = 11.sp)
        }
        LabCard {
            Text("CALIBRATION", color = teal, fontWeight = FontWeight.Bold)
            Text("Dead zone: " + (deadZone*100).roundToInt() + "%", color = paper)
            Slider(value = deadZone, onValueChange = { deadZone = it },
                valueRange = 0f..0.4f, enabled = current != null)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Invert vertical axis (future mapping)", modifier = Modifier.weight(1f),
                    color = paper, fontSize = 13.sp)
                Switch(checked = invert, onCheckedChange = { invert = it }, enabled = current != null)
            }
            Button(onClick = { current?.let { save(it.descriptor, Calibration(deadZone,invert)) } },
                enabled = current != null) { Text("Save calibration") }
            Text("Stored per controller. These settings will apply to the future mapping engine, not Android system input.",
                color = soft, fontSize = 11.sp)
        }
    }
}

@Composable
fun MappingStudio(games: List<SavedGame>, mappings: List<TouchBinding>,
    selectedGame: String?, selectGame: (String) -> Unit, add: (String, String) -> Unit,
    move: (Long, Float, Float) -> Unit, remove: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf<Long?>(null) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Mapping Studio", fontWeight = FontWeight.Bold, fontSize = 25.sp, color = paper)
        Text("Arrange controller buttons as touchscreen targets", color = soft, fontSize = 13.sp)
        if (games.isEmpty()) {
            LabCard { Text("Add a game in the Games tab to begin.", color = soft) }
        } else {
            var expanded by remember { mutableStateOf(false) }
            val label = games.firstOrNull { it.packageName == selectedGame }?.label ?: "Choose game"
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(label, modifier = Modifier.weight(1f)); Text("▾")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    games.forEach { game ->
                        DropdownMenuItem(text = { Text(game.label) },
                            onClick = { selectGame(game.packageName); active = null; expanded = false })
                    }
                }
            }
            Text("Drag targets around the preview area. Position saves on release.", color = soft, fontSize = 12.sp)
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).heightIn(min = 210.dp)
                .clip(RoundedCornerShape(16.dp)).background(Color(0xFF101A29))
                .border(1.dp, outline, RoundedCornerShape(16.dp))) {
                val w = constraints.maxWidth.toFloat()
                val h = constraints.maxHeight.toFloat()
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOUCH CANVAS", color = Color(0xFF506A80), fontWeight = FontWeight.Bold)
                    Text("Normalized position preview", color = Color(0xFF506A80), fontSize = 11.sp)
                }
                mappings.forEach { binding ->
                    key(binding.id) {
                        var local by remember(binding.id) { mutableStateOf(Offset(binding.x,binding.y)) }
                        LaunchedEffect(binding.x, binding.y) { local = Offset(binding.x,binding.y) }
                        Box(Modifier.offset {
                            IntOffset((local.x*w - 22.dp.toPx()).roundToInt(),
                                (local.y*h - 22.dp.toPx()).roundToInt())
                        }.size(44.dp).background(teal, CircleShape)
                            .border(if (active == binding.id) 3.dp else 1.dp,
                                if (active == binding.id) paper else Color(0xFF07232C), CircleShape)
                            .pointerInput(binding.id,w,h) {
                                detectDragGestures(onDragEnd = { move(binding.id,local.x,local.y) }) { change, drag ->
                                    change.consume()
                                    local = Offset((local.x+drag.x/w).coerceIn(.06f,.94f),
                                        (local.y+drag.y/h).coerceIn(.08f,.92f))
                                }
                            }.clickable { active = binding.id }, contentAlignment = Alignment.Center) {
                            Text(binding.button.take(5), color = Color(0xFF05252B),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showPicker = true }, enabled = selectedGame != null) { Text("+ Add button") }
                OutlinedButton(onClick = { active?.let(remove); active = null },
                    enabled = active != null) { Text("Delete selected") }
            }
            Text(mappings.size.toString() + " saved targets · Preview only. Touch injection is not active.",
                color = soft, fontSize = 11.sp)
        }
    }
    if (showPicker && selectedGame != null) AlertDialog(onDismissRequest = { showPicker = false },
        title = { Text("Add controller button") },
        text = { Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
            buttons.forEach { button ->
                Text(button, modifier = Modifier.fillMaxWidth().clickable {
                    add(selectedGame,button); showPicker = false
                }.padding(12.dp), color = paper)
            }
        } }, confirmButton = {
            TextButton(onClick = { showPicker = false }) { Text("Cancel") }
        })
}
