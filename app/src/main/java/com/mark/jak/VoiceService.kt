//package com.mark.jak
//
//import android.app.*
//import android.content.Intent
//import android.os.*
//import android.speech.*
//import android.speech.tts.TextToSpeech
//import android.util.Log
//import android.widget.TextView
//import androidx.core.app.NotificationCompat
//import java.util.*
//
//class VoiceService : Service(), RecognitionListener {
//
//    private lateinit var speechRecognizer: SpeechRecognizer
//    private lateinit var recognizerIntent: Intent
//    private lateinit var tts: TextToSpeech
//    private lateinit var commandManager: CommandManager
//
//
//    private val mainHandler = Handler(Looper.getMainLooper())
//
//    override fun onCreate() {
//        super.onCreate()
//
//        startForegroundNotification()
//        initTTS()
//
//        mainHandler.post {
//            initSpeechRecognizer()
//            startListening()
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        mainHandler.post {
//            speechRecognizer.destroy()
//        }
//        tts.shutdown()
//        super.onDestroy()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    /* ---------------- NOTIFICATION ---------------- */
//
//    private fun startForegroundNotification() {
//        val channelId = "voice_service_channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Jak Voice Assistant",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            getSystemService(NotificationManager::class.java)
//                .createNotificationChannel(channel)
//        }
//
//        val intent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Jak is listening")
//            .setContentText("Say a command")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentIntent(pendingIntent)
//            .setOngoing(true)
//            .build()
//
//        startForeground(1, notification)
//    }
//
//    /* ---------------- INIT ---------------- */
//
//    private fun initTTS() {
//        tts = TextToSpeech(this) {
//            if (it == TextToSpeech.SUCCESS) {
//                tts.language = Locale.getDefault()
//            }
//        }
//    }
//
//    private fun initSpeechRecognizer() {
//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
//        speechRecognizer.setRecognitionListener(this)
//
//        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
//        }
//
//        val dummyTextView = TextView(this)
//        commandManager = CommandManager(this, tts, dummyTextView)
//    }
//
//    private fun startListening() {
//        try {
//            speechRecognizer.startListening(recognizerIntent)
//        } catch (e: Exception) {
//            Log.e("Jak", "startListening failed", e)
//        }
//    }
//
//
//    /* ---------------- SPEECH CALLBACKS ---------------- */
//
//    override fun onResults(results: Bundle) {
//        val matches =
//            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//
//        val spokenText = matches?.firstOrNull() ?: return
//
//        Log.d("Jak", "Heard: $spokenText")
//        commandManager.execute(spokenText)
//
//        restartListening()
//    }
//
//    override fun onError(error: Int) {
//        Log.e("Jak", "Speech error: $error")
//
//        val delay = when (error) {
//            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 1500L
//            else -> 500L
//        }
//
//        mainHandler.postDelayed({
//            startListening()
//        }, delay)
//    }
//
//
//    private fun restartListening() {
//        mainHandler.postDelayed({
//            startListening()
//        }, 500)
//    }
//
//
//
//    override fun onReadyForSpeech(params: Bundle?) {}
//    override fun onBeginningOfSpeech() {}
//    override fun onRmsChanged(rmsdB: Float) {}
//    override fun onBufferReceived(buffer: ByteArray?) {}
//    override fun onEndOfSpeech() {}
//    override fun onPartialResults(partialResults: Bundle?) {}
//    override fun onEvent(eventType: Int, params: Bundle?) {}
//}







package com.mark.jak

import android.app.*
import android.content.Intent
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.util.*

class VoiceService : Service(), RecognitionListener {

    companion object {
        const val ACTION_PAUSE = "com.mark.jak.PAUSE"
        const val ACTION_RESUME = "com.mark.jak.RESUME"
        const val ACTION_STOP = "com.mark.jak.STOP"
        const val CHANNEL_ID = "voice_service_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_SERVICE_STOPPED = "com.mark.jak.SERVICE_STOPPED"

    }

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private lateinit var tts: TextToSpeech
    private lateinit var commandManager: CommandManager

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isListening = true

    /* ---------------- SERVICE LIFECYCLE ---------------- */

    override fun onCreate() {
        super.onCreate()

        initTTS()
        startForegroundNotification("Jak is listening")

        mainHandler.post {
            initSpeechRecognizer()
            startListening()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pauseListening()
            ACTION_RESUME -> resumeListening()
            ACTION_STOP -> stopServiceFully()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mainHandler.post {
            speechRecognizer.destroy()
        }
        tts.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /* ---------------- NOTIFICATION ---------------- */

    private fun startForegroundNotification(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Jak Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val pauseIntent = PendingIntent.getService(
            this, 0,
            Intent(this, VoiceService::class.java).setAction(ACTION_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = PendingIntent.getService(
            this, 1,
            Intent(this, VoiceService::class.java).setAction(ACTION_RESUME),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 2,
            Intent(this, VoiceService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Jak Voice Assistant")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .addAction(
                if (isListening)
                    NotificationCompat.Action(0, "Pause", pauseIntent)
                else
                    NotificationCompat.Action(0, "Resume", resumeIntent)
            )
            .addAction(NotificationCompat.Action(0, "Stop", stopIntent))
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(text: String) {
        startForegroundNotification(text)
    }

    /* ---------------- INIT ---------------- */

    private fun initTTS() {
        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale.getDefault()
            }
        }
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        commandManager = CommandManager(this, tts, TextView(this))
    }

    /* ---------------- LISTEN CONTROL ---------------- */

    private fun startListening() {
        if (!isListening) return
        try {
            speechRecognizer.startListening(recognizerIntent)
        } catch (e: Exception) {
            Log.e("Jak", "startListening failed", e)
        }
    }

    private fun pauseListening() {
        if (!isListening) return
        isListening = false
        speechRecognizer.stopListening()
        tts.speak("Okay, I will stop listening",
            TextToSpeech.QUEUE_FLUSH, null, null)
        updateNotification("Jak is paused")
    }

    private fun resumeListening() {
        if (isListening) return
        isListening = true
        startListening()
        tts.speak("I am listening again",
            TextToSpeech.QUEUE_FLUSH, null, null)
        updateNotification("Jak is listening")
    }

    private fun stopServiceFully() {
        tts.speak("Goodbye", TextToSpeech.QUEUE_FLUSH, null, null)

        sendBroadcast(
            Intent(ACTION_SERVICE_STOPPED)
        )

        stopForeground(true)
        stopSelf()
    }


    /* ---------------- SPEECH CALLBACKS ---------------- */

    override fun onResults(results: Bundle) {
        val spokenText =
            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull() ?: return

        Log.d("Jak", "Heard: $spokenText")
        commandManager.execute(spokenText)

        restartListening()
    }

    override fun onError(error: Int) {
        if (!isListening) return
        mainHandler.postDelayed({ startListening() }, 600)
    }

    private fun restartListening() {
        if (!isListening) return
        mainHandler.postDelayed({ startListening() }, 500)
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}