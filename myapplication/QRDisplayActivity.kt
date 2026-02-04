package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent // Intent error-ah fix panna idhu mukhkiyam
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.telephony.SmsManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File

class QRDisplayActivity : AppCompatActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_display)

        checkPermissionsAndStart()
    }

    private fun checkPermissionsAndStart() {
        val perms = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )

        if (perms.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            executeSOS()
        } else {
            ActivityCompat.requestPermissions(this, perms, 123)
        }
    }

    private fun executeSOS() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val mapsLink = "https://www.google.com/maps?q=${loc.latitude},${loc.longitude}"

                    val sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val name = sp.getString("name", "User")
                    val emergencyNum = sp.getString("father", "")

                    // Offline SMS with all details
                    val smsMsg = "🚨 SOS! $name is in trouble. Location: $mapsLink"
                    sendOfflineSMS(emergencyNum ?: "", smsMsg)

                    // QR Data with all details
                    val qrData = "🚨 EMERGENCY 🚨\nName: $name\nContact: $emergencyNum\nLocation: $mapsLink"
                    generateQR(qrData)

                    // Voice Record
                    startVoiceRecording()
                } else {
                    Toast.makeText(this, "Location kidaikala! GPS ON pannunga.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendOfflineSMS(number: String, msg: String) {
        if (number.isEmpty()) return
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(number, null, msg, null, null)
            Toast.makeText(this, "SMS Sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "SMS Fail!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateQR(details: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(details, BarcodeFormat.QR_CODE, 600, 600)
            findViewById<ImageView>(R.id.ivQRCode).setImageBitmap(bitmap)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun startVoiceRecording() {
        try {
            audioFile = File(getExternalFilesDir(null), "SOS_Recording.3gp")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            Handler(Looper.getMainLooper()).postDelayed({ stopAndShare() }, 60000)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun stopAndShare() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null

            val uri = FileProvider.getUriForFile(this, "$packageName.provider", audioFile!!)

            // Fixed Intent Logic
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "audio/3gp"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(shareIntent, "Share SOS Voice Record"))
        } catch (e: Exception) { e.printStackTrace() }
    }
}