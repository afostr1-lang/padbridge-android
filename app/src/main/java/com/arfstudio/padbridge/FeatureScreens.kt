package com.arfstudio.padbridge

import android.view.MotionEvent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
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

private val surface = PbSurface
private val outline = PbStroke
private val teal = PbAccent
private val soft = PbMuted
private val paper = PbText
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
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Controller Lab", fontSize = 25.sp, color = paper, fontWeight = FontWeight.Bold)
        Text("Test inputs, tune analog sensitivity, and save settings", color = soft)
        LabCard {
            Text(if (current == null) "No controller connected" else "● " + current.name,
                color = teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Pair the controller in Android Bluetooth settings, then return here.", color = soft, fontSize = 13.sp)
        }
        LabCard {
            Text("LIVE INPUT", color = teal, fontWeight = FontWeight.Bold)
            ControllerVisualizer(axes)
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
        Text("Touch control layout editor", color = soft, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Default.Layers, null, tint = teal, modifier = Modifier.size(16.dp))
            Text("GAME PROFILE", color = teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("PREVIEW MODE", color = soft, fontSize = 10.sp)
        }
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
            Text("Place each mapped button over its matching on-screen control.", color = soft, fontSize = 12.sp)
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).heightIn(min = 210.dp)
                .clip(RoundedCornerShape(16.dp)).background(Color(0xFF101A29))
                .border(1.dp, outline, RoundedCornerShape(16.dp))) {
                val w = constraints.maxWidth.toFloat()
                val h = constraints.maxHeight.toFloat()
                Canvas(Modifier.matchParentSize()) {
                    val grid = Color(0xFF264056)
                    for (i in 1..7) {
                        val x = size.width*i/8f
                        drawLine(grid.copy(alpha=.35f), Offset(x,0f), Offset(x,size.height), strokeWidth=1f)
                    }
                    for (i in 1..5) {
                        val y = size.height*i/6f
                        drawLine(grid.copy(alpha=.35f), Offset(0f,y), Offset(size.width,y), strokeWidth=1f)
                    }
                }
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOUCH CANVAS", color = Color(0xFF506A80), fontWeight = FontWeight.Bold)
                    Text("Drag to arrange · tap to select", color = Color(0xFF506A80), fontSize = 11.sp)
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
            Text(mappings.size.toString() + " targets saved · In-game mapping engine is not active yet.",
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

@Composable
private fun ControllerVisualizer(axes: Map<Int, Float>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        AnalogDisplay("LEFT STICK", axes[MotionEvent.AXIS_X] ?: 0f, axes[MotionEvent.AXIS_Y] ?: 0f)
        AnalogDisplay("RIGHT STICK", axes[MotionEvent.AXIS_Z] ?: 0f, axes[MotionEvent.AXIS_RZ] ?: 0f)
    }
}

@Composable
private fun AnalogDisplay(label: String, x: Float, y: Float) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.size(105.dp)) {
            val center = Offset(size.width/2f,size.height/2f)
            val radius = size.minDimension*.44f
            drawCircle(Color(0xFF0D192B),radius)
            drawCircle(PbStroke,radius,style=Stroke(width=3f))
            drawLine(PbStroke,Offset(center.x-radius,center.y),Offset(center.x+radius,center.y))
            drawLine(PbStroke,Offset(center.x,center.y-radius),Offset(center.x,center.y+radius))
            drawCircle(PbAccent.copy(alpha=.24f),radius*.34f,
                center+Offset(x.coerceIn(-1f,1f)*radius*.78f,y.coerceIn(-1f,1f)*radius*.78f))
            drawCircle(PbAccent,radius*.18f,
                center+Offset(x.coerceIn(-1f,1f)*radius*.78f,y.coerceIn(-1f,1f)*radius*.78f))
        }
        Text(label, color = PbMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
