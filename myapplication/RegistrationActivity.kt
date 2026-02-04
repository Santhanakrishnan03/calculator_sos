package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val btnSave = findViewById<Button>(R.id.btnSave)
        val etName = findViewById<EditText>(R.id.etName)

        val etAge = findViewById<EditText>(R.id.etAge)
        val etBlood = findViewById<EditText>(R.id.etBlood)
        // -------------------------------------------------------

        val etFather = findViewById<EditText>(R.id.etFather)
        val etMother = findViewById<EditText>(R.id.etMother)
        val etHusband = findViewById<EditText>(R.id.etHusband)
        val etFriend = findViewById<EditText>(R.id.etFriend)

        btnSave.setOnClickListener {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().apply {
                putString("user_name", etName.text.toString())

                putString("user_age", etAge.text.toString())   // <- etName-ku badhula etAge irukanum
                putString("user_blood", etBlood.text.toString()) // <- etName-ku badhula etBlood irukanum

                putString("father_num", etFather.text.toString())
                putString("mother_num", etMother.text.toString())
                putString("husband_num", etHusband.text.toString())
                putString("friend_num", etFriend.text.toString())
                putBoolean("isRegistered", true)
                apply()
            }
            Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}