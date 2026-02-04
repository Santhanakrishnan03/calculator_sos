package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class CalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        val btnQR = findViewById<Button>(R.id.btnQR)
        btnQR.setOnClickListener {
            generateQR()
        }
    }

    private fun generateQR() {
        val sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var locationLink = "GPS OFF"

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val loc: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            loc?.let {
                locationLink = "https://www.google.com/maps?q=${it.latitude},${it.longitude}"
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        val qrData = """
            🚨 SOS DETAILS 🚨
            Name: ${sp.getString("name", "")}
            Age: ${sp.getString("age", "")}
            Blood: ${sp.getString("blood", "")}
            Address: ${sp.getString("address", "")}
            Contact: ${sp.getString("parent", "")}
            Location: $locationLink
        """.trimIndent()

        try {
            val encoder = BarcodeEncoder()
            val bitmap: Bitmap = encoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 600, 600)
            val iv = ImageView(this)
            iv.setImageBitmap(bitmap)

            AlertDialog.Builder(this)
                .setTitle("Your Emergency QR")
                .setView(iv)
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to generate QR", Toast.LENGTH_SHORT).show()
        }
    }
}