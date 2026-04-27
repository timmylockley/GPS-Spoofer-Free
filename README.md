# OSM SpoofMaster 📍

A lightweight, simple Android application written in Kotlin that allows users to mock their GPS location using OpenStreetMap (OSM).

## ✨ Features
* **Interactive Map:** Powered by `osmdroid`, no Google API keys required.
* **Tap-to-Point:** Simply tap anywhere on the map to set your target coordinates.
* **Persistent Spoofing:** Uses a background thread to maintain the mock location.
* **Material UI:** Clean interface with Start/Stop controls.

## 🚀 How to Use
1. **Enable Developer Options:** Go to `Settings > About Phone` and tap `Build Number` 7 times.
2. **Select Mock Location App:** Go to `Settings > System > Developer Options > Select mock location app` and choose **OSM Spoofer**.
3. **Grant Permissions:** Open the app and allow Location permissions when prompted.
4. **Spoof:** Tap the map to drop a pin and hit **START**.

## 🛠️ Technical Setup
* **Language:** Kotlin
* **Map Engine:** [osmdroid](https://github.com/osmdroid/osmdroid)
* **API Level:** Min SDK 24 (Android 7.0)

## ⚠️ Disclaimer
This project is for educational and testing purposes only. Using location spoofing may violate the Terms of Service of certain apps (e.g., games or navigation apps). Use responsibly.
