package com.mark.jak

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.TextView
import android.widget.Toast

class CommandManager(
    private val context: Context,
    private val tts: TextToSpeech,
    private val statusText: TextView
) {

    fun execute(command: String) {
        val lowerCommand = command.lowercase()

        when {
            lowerCommand.contains("turn on") && lowerCommand.contains("flash") -> {
                controlFlashlight(true)
                speak("Flashlight turned on")
            }

            lowerCommand.contains("turn off") && lowerCommand.contains("flash") -> {
                controlFlashlight(false)
                speak("Flashlight turned off")
            }

            // Placeholder for future commands
            lowerCommand.contains("turn on") && lowerCommand.contains("wifi") -> {
                speak("Wi-Fi control coming soon")
            }

            else -> {
                speak("Sorry, I didn't understand")
            }
        }
    }

    private fun controlFlashlight(on: Boolean) {
        val hasFlash = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlash) {
            speak("This device does not support flashlight")
            statusText.text = "No flashlight available"
            return
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, on)
                statusText.text = if (on) "Flashlight ON" else "Flashlight OFF"
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Flashlight error", Toast.LENGTH_SHORT).show()
            statusText.text = "Flashlight error"
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
