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

## 🧱 Architecture

