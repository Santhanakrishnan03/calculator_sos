package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var tvDisplay: TextView
    private var firstVal = 0.0
    private var operator = ""
    private var isNew = true

    // Audio Variables
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        if (!sp.getBoolean("isRegistered", false)) {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        tvDisplay = findViewById(R.id.tvDisplay)

        // Request Permissions (SMS, Location, Mic)
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        ), 1)

        setupCalculatorButtons()

        val btnAC = findViewById<Button>(R.id.btnAC)
        btnAC.setOnLongClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            true
        }

        val btnEqual = findViewById<Button>(R.id.btnEqual)
        btnEqual.setOnClickListener {
            if (tvDisplay.text.toString() == "0") {
                startEmergencyProtocol()
            } else {
                calculateResult()
            }
        }
    }

    private fun setupCalculatorButtons() {
        val nums = listOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot)
        for (id in nums) {
            findViewById<Button>(id).setOnClickListener {
                if (isNew) tvDisplay.text = ""
                isNew = false
                tvDisplay.append((it as Button).text)
            }
        }
        findViewById<Button>(R.id.btnAdd).setOnClickListener { setOp("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { setOp("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { setOp("×") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { setOp("÷") }
        findViewById<Button>(R.id.btnAC).setOnClickListener {
            tvDisplay.text = "0"
            isNew = true
        }
    }

    private fun setOp(op: String) {
        firstVal = tvDisplay.text.toString().toDoubleOrNull() ?: 0.0
        operator = op
        isNew = true
    }

    private fun calculateResult() {
        val secondVal = tvDisplay.text.toString().toDoubleOrNull() ?: 0.0
        var res = 0.0
        when (operator) {
            "+" -> res = firstVal + secondVal
            "-" -> res = firstVal - secondVal
            "×" -> res = firstVal * secondVal
            "÷" -> if (secondVal != 0.0) res = firstVal / secondVal
        }
        tvDisplay.text = if (res % 1 == 0.0) res.toInt().toString() else res.toString()
        isNew = true
    }

    private fun startEmergencyProtocol() {
        startRecording()

        fetchLocationAndSendSOS()

        Handler(Looper.getMainLooper()).postDelayed({
            stopRecording()
            Toast.makeText(this, "SOS Audio (1 Min) Saved", Toast.LENGTH_SHORT).show()
        }, 60000)
    }

    private var currentAudioUri: android.net.Uri? = null

    private fun startRecording() {
        try {
            val resolver = contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "SOS_${System.currentTimeMillis()}.mp3")
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                // Idhu dhaan 'Music' folder-la 'SOS_Recordings' nu oru folder create pannum
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/SOS_Recordings")
            }

            currentAudioUri = resolver.insert(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            currentAudioUri?.let { uri ->
                val pfd = resolver.openFileDescriptor(uri, "w")
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(pfd?.fileDescriptor)
                    prepare()
                    start()
                }
                Toast.makeText(this, "Recording started (Public Music Folder)", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
private fun stopRecording() {
    try {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Toast.makeText(this, "Audio saved to Music/SOS_Recordings", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
    private fun fetchLocationAndSendSOS() {
        val sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->

                // 1. Google Maps Link
                val locationLink = if (location != null) {
                    "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                } else {
                    "Location Not Available"
                }

                // 2. Fetching ALL Details from SharedPrefs
                // Inga irukkira Keys unga Registration logic-oda match aaganum
                val name = sp.getString("user_name", "N/A")
                val age = sp.getString("user_age", "N/A")
                val blood = sp.getString("user_blood", "N/A")
                val father = sp.getString("father_num", "N/A")
                val mother = sp.getString("mother_num", "N/A")
                val husband = sp.getString("husband_num", "N/A")
                val friend = sp.getString("friend_num", "N/A")

                val fullMessage = """
                🚨 EMERGENCY SOS 🚨
                ------------------
                Name: $name
                Age: $age
                Blood Group: $blood
                
                Emergency Contacts:
                Father: $father
                Mother: $mother
                Husband: $husband   
                Friend: $friend
                
                Live Location:
                $locationLink
                ------------------
                (1-Minute Audio Recording Started!)
            """.trimIndent()

                showQRDialog(fullMessage)
                sendSMS(fullMessage)

                if (location == null) {
                    Toast.makeText(this, "Turn on GPS for location!", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Location Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showQRDialog(content: String) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        val imageView = ImageView(this)
        imageView.setImageBitmap(bmp)
        AlertDialog.Builder(this).setTitle("Emergency SOS QR").setView(imageView).setPositiveButton("Close", null).show()
    }

    private fun sendSMS(message: String) {
        val sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val smsManager = SmsManager.getDefault()
        val numbers = listOf("father_num", "mother_num", "husband_num", "friend_num")

        for (key in numbers) {
            val num = sp.getString(key, "")
            if (!num.isNullOrEmpty()) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(num, null, parts, null, null)
            }
        }
        Toast.makeText(this, "Danger Alert Sent!", Toast.LENGTH_LONG).show()
    }
}