//package com.mark.jak
//
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.hardware.camera2.CameraManager
//import android.media.AudioManager
//import android.net.wifi.WifiManager
//import android.os.Build
//import android.provider.Settings
//import android.speech.tts.TextToSpeech
//import android.util.Log
//import android.widget.TextView
//import android.widget.Toast
//
//class CommandManager(
//    private val context: Context,
//    private val tts: TextToSpeech,
//    private val statusText: TextView
//) {
//
//    fun execute(spokenText: String) {
//        val command = spokenText.lowercase()
//        Log.d("Jak", "CommandManager received: $command")
//
//        when {
//            command.contains("turn on") && command.contains("flash") ->
//                flashlight(true)
//
//            command.contains("turn off") && command.contains("flash") ->
//                flashlight(false)
//
//            command.contains("volume up") ->
//                changeVolume(AudioManager.ADJUST_RAISE, "Volume increased")
//
//            command.contains("volume down") ->
//                changeVolume(AudioManager.ADJUST_LOWER, "Volume decreased")
//
//            command.contains("mute volume") || command.contains("mute sound") ->
//                muteVolume()
//
//            command.contains("turn on wireless") || command.contains("enable wireless") ->
//                setWifi(true)
//
//            command.contains("turn off wireless") || command.contains("disable wireless") ->
//                setWifi(false)
//
//            else -> {
//                speak("Sorry, I didn't understand")
//                statusText.text = "Unknown command"
//            }
//        }
//    }
//
//    /* ---------------- FLASHLIGHT ---------------- */
//
//    private fun flashlight(on: Boolean) {
//        val hasFlash = context.packageManager
//            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
//
//        if (!hasFlash) {
//            speak("This device does not support flashlight")
//            statusText.text = "No flashlight available"
//            return
//        }
//
//        try {
//            val cameraManager =
//                context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//            val cameraId = cameraManager.cameraIdList.first()
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                cameraManager.setTorchMode(cameraId, on)
//                val msg = if (on) "Flashlight turned on" else "Flashlight turned off"
//                speak(msg)
//                statusText.text = msg
//            }
//        } catch (e: Exception) {
//            Log.e("Jak", "Flashlight error", e)
//            speak("Flashlight error")
//            statusText.text = "Flashlight error"
//        }
//    }
//
//    /* ---------------- VOLUME ---------------- */
//
//    private fun changeVolume(direction: Int, message: String) {
//        val audioManager =
//            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//        audioManager.adjustVolume(direction, AudioManager.FLAG_SHOW_UI)
//        speak(message)
//        statusText.text = message
//    }
//
//    private fun muteVolume() {
//        val audioManager =
//            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//        audioManager.adjustVolume(AudioManager.ADJUST_MUTE, 0)
//        speak("Volume muted")
//        statusText.text = "Volume muted"
//    }
//
//    /* ---------------- WIFI ---------------- */
//
//    private fun setWifi(enable: Boolean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // Android 10+ does NOT allow apps to toggle Wi-Fi directly
//            speak("Please enable WiFi manually")
//            statusText.text = "Opening WiFi settings"
//
//            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            context.startActivity(intent)
//            return
//        }
//
//        val wifiManager =
//            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//
//        wifiManager.isWifiEnabled = enable
//        val msg = if (enable) "WiFi turned on" else "WiFi turned off"
//        speak(msg)
//        statusText.text = msg
//    }
//
//    /* ---------------- SPEECH ---------------- */
//
//    private fun speak(text: String) {
//        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//    }
//}





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

class CommandManager(
    private val context: Context,
    private val tts: TextToSpeech,
    private val statusText: TextView
) {

    // 🔹 Store last volume before muting
    private var lastVolumeLevel: Int = -1

    fun execute(spokenText: String) {
        val command = spokenText.lowercase()
        Log.d("Jak", "CommandManager received: $command")

        when {
            command.contains("turn on") && command.contains("flash") ->
                flashlight(true)

            command.contains("turn off") && command.contains("flash") ->
                flashlight(false)

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

            command.contains("turn on wireless") || command.contains("enable wireless") ->
                setWifi(true)

            command.contains("turn off wireless") || command.contains("disable wireless") ->
                setWifi(false)

            command.contains("turn on bluetooth") ||
                    command.contains("enable bluetooth") ->
                setBluetooth(true)

            command.contains("turn off bluetooth") ||
                    command.contains("disable bluetooth") ->
                setBluetooth(false)

            command.contains("battery") ||
                    command.contains("battery level") ||
                    command.contains("power level") ||
                    command.contains("how much battery") ||
                    command.contains("how much power") -> {
                checkBatteryLevel()
            }



            else -> {
                speak("Sorry, I didn't understand")
                statusText.text = "Unknown command"
            }
        }
    }

    /* ---------------- FLASHLIGHT ---------------- */

    private fun flashlight(on: Boolean) {
        val hasFlash = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (!hasFlash) {
            speak("This device does not support flashlight")
            statusText.text = "No flashlight available"
            return
        }

        try {
            val cameraManager =
                context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.first()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, on)
                val msg = if (on) "Flashlight turned on" else "Flashlight turned off"
                speak(msg)
                statusText.text = msg
            }
        } catch (e: Exception) {
            Log.e("Jak", "Flashlight error", e)
            speak("Flashlight error")
            statusText.text = "Flashlight error"
        }
    }

    /* ---------------- VOLUME ---------------- */

    private fun changeVolume(direction: Int, message: String) {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.adjustVolume(direction, AudioManager.FLAG_SHOW_UI)
        speak(message)
        statusText.text = message
    }

    private fun muteVolume() {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Save current volume only once
        if (lastVolumeLevel == -1) {
            lastVolumeLevel =
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            0,
            AudioManager.FLAG_SHOW_UI
        )

        speak("Volume muted")
        statusText.text = "Volume muted"
    }

    private fun unmuteVolume() {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val restoreLevel = if (lastVolumeLevel > 0) {
            lastVolumeLevel
        } else {
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2
        }

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            restoreLevel,
            AudioManager.FLAG_SHOW_UI
        )

        lastVolumeLevel = -1

        speak("Volume restored")
        statusText.text = "Volume restored"
    }

    /* ---------------- WIFI ---------------- */

    private fun setWifi(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ restriction
            speak("Please enable WiFi manually")
            statusText.text = "Opening WiFi settings"

            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }

        val wifiManager =
            context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiManager.isWifiEnabled = enable
        val msg = if (enable) "WiFi turned on" else "WiFi turned off"
        speak(msg)
        statusText.text = msg
    }




    /* ---------------- BLUETOOTH ---------------- */

    private fun setBluetooth(enable: Boolean) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            speak("This device does not support Bluetooth")
            statusText.text = "Bluetooth not supported"
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ restriction
            val action = if (enable)
                "Please turn on Bluetooth"
            else
                "Please turn off Bluetooth"

            speak(action)
            statusText.text = "Opening Bluetooth settings"

            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }

        // Android 9 and below
        if (enable && !bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
            speak("Bluetooth turned on")
            statusText.text = "Bluetooth on"
        } else if (!enable && bluetoothAdapter.isEnabled) {
            bluetoothAdapter.disable()
            speak("Bluetooth turned off")
            statusText.text = "Bluetooth off"
        } else {
            val msg = if (enable) "Bluetooth is already on" else "Bluetooth is already off"
            speak(msg)
            statusText.text = msg
        }
    }


    /* ---------------- BATTERY ---------------- */

    private fun checkBatteryLevel() {
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        if (batteryIntent == null) {
            speak("Unable to get battery level")
            statusText.text = "Battery info unavailable"
            return
        }

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        if (level == -1 || scale == -1) {
            speak("Unable to read battery level")
            statusText.text = "Battery read error"
            return
        }

        val batteryPercent = (level * 100) / scale
        val message = "Battery level is $batteryPercent percent"

        speak(message)
        statusText.text = message
    }



    /* ---------------- SPEECH ---------------- */

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
