package com.arfstudio.padbridge

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import java.util.Locale

data class ControllerInfo(val id: Int, val name: String, val descriptor: String, val axes: Int)

/** Lists Android-recognized controllers; does not capture input from other apps. */
class ControllerMonitor(context: Context, private val onChanged: () -> Unit) : InputManager.InputDeviceListener {
    private val manager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    fun start() = manager.registerInputDeviceListener(this, null)
    fun stop() = manager.unregisterInputDeviceListener(this)
    override fun onInputDeviceAdded(deviceId: Int) = onChanged()
    override fun onInputDeviceRemoved(deviceId: Int) = onChanged()
    override fun onInputDeviceChanged(deviceId: Int) = onChanged()
    fun connected(): List<ControllerInfo> = InputDevice.getDeviceIds().toList().mapNotNull { id ->
        val device = InputDevice.getDevice(id) ?: return@mapNotNull null
        val gamepad = device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
            device.sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
        if (gamepad && !device.isVirtual) ControllerInfo(id, device.name, device.descriptor, device.motionRanges.size) else null
    }
}

fun isControllerKey(event: KeyEvent): Boolean =
    event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
    event.source and InputDevice.SOURCE_DPAD == InputDevice.SOURCE_DPAD ||
    event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK

fun joystickSummary(event: MotionEvent): String {
    val device = event.device ?: return "Unknown controller"
    return device.motionRanges.filter {
        it.source and InputDevice.SOURCE_CLASS_JOYSTICK != 0
    }.distinctBy { it.axis }.joinToString("  ") { range ->
        "${MotionEvent.axisToString(range.axis)}: ${String.format(Locale.US, "%.2f", event.getAxisValue(range.axis))}"
    }.ifBlank { "No joystick axes reported" }
}
