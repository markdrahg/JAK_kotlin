# 🤖 Jak — Offline Voice Assistant (v1)

Jak is an **offline-first Android voice assistant** built with **Kotlin**, designed to continuously listen for voice commands using Android’s native `SpeechRecognizer` while running as a **foreground service**.

Version 1 focuses on **core voice control**, **service lifecycle stability**, and a **clean command architecture**, providing a solid foundation for future intelligent features.

---

## ✨ Features

- 🎙️ **Continuous Voice Listening**
  - Uses Android’s native `SpeechRecognizer`
  - Automatically restarts listening after each command

- 🔊 **Text-to-Speech Feedback**
  - Uses `TextToSpeech` for spoken responses
  - Confirms actions like pause, resume, and stop

- ⏸️ **Pause & Resume Listening**
  - Pause listening without stopping the service
  - Resume listening via notification actions

- 🛑 **Stop Listening Completely**
  - Fully shuts down the voice service
  - Notifies the UI when the service stops

- 📢 **Foreground Service**
  - Runs reliably in the background
  - Persistent notification with control actions

- 🧠 **Command-Based Architecture**
  - Centralized command handling via `CommandManager`
  - Easy to extend with new voice commands

---

## 🧱 Architecture (high level)

Jak is structured around a long-running **foreground service** that manages the speech recognition lifecycle and routes recognized utterances into a command layer.

Typical flow:

1. **ForegroundService** starts and posts a persistent notification (required for reliability).
2. The service creates and configures Android’s **`SpeechRecognizer`**.
3. Recognition callbacks provide partial/final results.
4. The recognized text is passed to **`CommandManager`**, which:
   - matches text against known commands
   - invokes the appropriate handler
5. Jak speaks back via **`TextToSpeech`** to confirm what happened.
6. After each command/result, the recognizer is restarted (when not paused) to keep listening.

This separation keeps “Android lifecycle + microphone” concerns in the service layer, and keeps “what does this phrase do?” logic in the command layer.

---

## ✅ Getting started

### Prerequisites

- Android Studio (latest stable recommended)
- JDK compatible with your Android Gradle Plugin (commonly JDK 17)
- An Android device or emulator (note: mic/recognition behavior is typically better on a real device)

### Build / install

From the repo root:

```bash
./gradlew assembleDebug
```

To install to a connected device:

```bash
./gradlew installDebug
```

---

## 🔐 Permissions & platform notes

Because Jak listens for voice input in the background, it commonly requires:

- `RECORD_AUDIO`
- Foreground service + notification permissions/requirements depending on Android version

Notes:

- Android’s `SpeechRecognizer` quality/availability can vary by device and installed speech services.
- “Offline-first” can still depend on the device’s speech recognition engine and language packs.
- Continuous listening generally means carefully handling:
  - service restarts
  - audio focus
  - recognizer errors/timeouts

---

## 📁 Project structure (typical)

Most Android/Kotlin projects follow a layout like:

- `app/src/main/java|kotlin/...` — app source
- `app/src/main/res/...` — resources
- `app/src/test/...` / `app/src/androidTest/...` — tests

---

## 🧭 Roadmap ideas

- More commands + better intent matching (while staying offline)
- Improved error recovery for `SpeechRecognizer`
- Optional on-device NLP/intent engine
- Configurable wake words / activation flow

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a pull request

---

## License

If you plan to open source this project, add a `LICENSE` file and update this section.
