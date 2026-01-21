package com.mark.jak

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import android.bluetooth.BluetoothAdapter
import android.content.IntentFilter
import android.os.BatteryManager
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale

class CommandManager(
    private val context: Context,
    private val tts: TextToSpeech,
    private val statusText: TextView
) {

    private var lastVolumeLevel: Int = -1

    fun execute(spokenText: String) {
        val command = spokenText.lowercase()
        Log.d("Jak", "CommandManager received: $command")

        when {

            /* -------- ASSISTANT CONTROL -------- */

            command.contains("stop listening") ||
                    command.contains("stop assistant") ||
                    command.contains("shutdown assistant") -> {
                speak("Stopping assistant")
                controlService(VoiceService.ACTION_STOP)
            }

            command.contains("pause listening") ||
                    command.contains("pause assistant") -> {
                speak("Pausing listening")
                controlService(VoiceService.ACTION_PAUSE)
            }

            command.contains("resume listening") ||
                    command.contains("wake up") ||
                    command.contains("start listening") -> {
                speak("I'm listening")
                controlService(VoiceService.ACTION_RESUME)
            }

            /* -------- FLASHLIGHT -------- */

            command.contains("turn on") && command.contains("flash") ->
                flashlight(true)

            command.contains("turn off") && command.contains("flash") ->
                flashlight(false)

            /* -------- VOLUME -------- */

            command.contains("volume up") ->
                changeVolume(AudioManager.ADJUST_RAISE, "Volume increased")

            command.contains("volume down") ->
                changeVolume(AudioManager.ADJUST_LOWER, "Volume decreased")

            command.contains("mute volume") || command.contains("mute sound") ->
                muteVolume()

            command.contains("unmute") ||
                    command.contains("restore volume") ||
                    command.contains("sound on") ->
                unmuteVolume()

            /* -------- WIFI -------- */

            command.contains("turn on wireless") ||
                    command.contains("enable wireless") ->
                setWifi(true)

            command.contains("turn off wireless") ||
                    command.contains("disable wireless") ->
                setWifi(false)

            /* -------- BLUETOOTH -------- */

            command.contains("turn on bluetooth") ||
                    command.contains("enable bluetooth") ->
                setBluetooth(true)

            command.contains("turn off bluetooth") ||
                    command.contains("disable bluetooth") ->
                setBluetooth(false)

            /* -------- BATTERY -------- */

            command.contains("battery") ||
                    command.contains("battery level") ||
                    command.contains("power level") ->
                checkBatteryLevel()

            /* -------- TIME / DATE -------- */

            command.contains("time") ->
                speakCurrentTime()

            command.contains("date") ||
                    command.contains("day") ->
                speakCurrentDate()

            else -> {
                speak("Sorry, I didn't understand")
                statusText.text = "Unknown command"
            }
        }
    }

    /* ================= SERVICE CONTROL ================= */

    private fun controlService(action: String) {
        val intent = Intent(context, VoiceService::class.java)
        intent.action = action
        context.startService(intent)
    }

    /* ================= FLASHLIGHT ================= */

    private fun flashlight(on: Boolean) {
        val hasFlash = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (!hasFlash) {
            speak("This device does not support flashlight")
            return
        }

        try {
            val cameraManager =
                context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.first()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, on)
                speak(if (on) "Flashlight turned on" else "Flashlight turned off")
            }
        } catch (e: Exception) {
            speak("Flashlight error")
        }
    }

    /* ================= VOLUME ================= */

    private fun changeVolume(direction: Int, message: String) {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(direction, AudioManager.FLAG_SHOW_UI)
        speak(message)
    }

    private fun muteVolume() {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (lastVolumeLevel == -1) {
            lastVolumeLevel =
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI
        )

        speak("Volume muted")
    }

    private fun unmuteVolume() {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val restoreLevel =
            if (lastVolumeLevel > 0) lastVolumeLevel
            else audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC, restoreLevel, AudioManager.FLAG_SHOW_UI
        )

        lastVolumeLevel = -1
        speak("Volume restored")
    }

    /* ================= WIFI ================= */

    private fun setWifi(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            speak("Please enable WiFi manually")
            context.startActivity(
                Intent(Settings.ACTION_WIFI_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }

        val wifiManager =
            context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiManager.isWifiEnabled = enable
        speak(if (enable) "WiFi turned on" else "WiFi turned off")
    }

    /* ================= BLUETOOTH ================= */

    private fun setBluetooth(enable: Boolean) {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: run {
            speak("Bluetooth not supported")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            speak("Please change Bluetooth manually")
            context.startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }

        if (enable) adapter.enable() else adapter.disable()
        speak(if (enable) "Bluetooth turned on" else "Bluetooth turned off")
    }

    /* ================= BATTERY ================= */

    private fun checkBatteryLevel() {
        val intent = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        val percent = (level * 100) / scale
        speak("Battery level is $percent percent")
    }

    /* ================= TIME / DATE ================= */

    private fun speakCurrentTime() {
        val now = Calendar.getInstance()
        speak("The time is ${now.get(Calendar.HOUR_OF_DAY)} ${now.get(Calendar.MINUTE)}")
    }

    private fun speakCurrentDate() {
        val date = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
        speak("Today is $date")
    }

    /* ================= SPEECH ================= */

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
