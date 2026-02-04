tealth SOS Calculator 📱🛡️
Stealth SOS Calculator is a personal safety Android application cleverly disguised as a fully functional calculator. It allows users to trigger emergency protocols discreetly without alerting anyone nearby.

✨ Key Features
Stealth Trigger: Disguised as a standard calculator. Entering 0 and pressing = immediately activates the emergency protocol.

Emergency QR Code: Generates a real-time QR code displaying the user's critical info (Name, Age, Blood Group) and emergency contact details for first responders.

Automatic SOS SMS: Sends a detailed emergency message to 4 pre-registered contacts, including a Live Google Maps location link.

Background Audio Recording: Automatically records 1 minute of audio in the background as evidence, saved directly to the device's public Music/SOS_Recordings folder.

Offline Capability: Designed to work even with minimal internet (SMS and Audio recording functions).

🚀 How It Works
Registration: On first launch, the user registers their personal details and 4 emergency contact numbers (Father, Mother, Husband/Wife, Friend).

The Mask: The app opens as a normal calculator for daily use.

The Emergency:

User types 0.

User presses =.

Result: The app immediately triggers SMS, displays the Emergency QR, and starts recording audio silently.

🛠 Tech Stack
Language: Kotlin

UI: XML (Material Design)

Location: Google Play Services Location API

QR Generation: ZXing (Zebra Crossing) Library

Storage: MediaStore API (for Scannable Audio files)

Data Persistence: SharedPreferences

📂 Project Structure
Plaintext
├── app/src/main/
│   ├── java/com/example/myapplication/
│   │   ├── MainActivity.kt          # Main Calculator & SOS Logic
│   │   └── RegistrationActivity.kt  # User Onboarding & Data Save
│   ├── res/layout/
│   │   ├── activity_main.xml        # Calculator UI
│   │   └── activity_registration.xml # User Form UI
│   └── AndroidManifest.xml          # Permissions (SMS, GPS, Mic)
└── build.gradle                     # Project Dependencies
⚠️ Permissions Required
To function correctly, the app requires:

SEND_SMS

ACCESS_FINE_LOCATION

RECORD_AUDIO

WRITE_EXTERNAL_STORAGE (for older Android versions)

👨‍💻 Developer
Developed with focus on Personal Safety and Privacy.# calculator_sos
