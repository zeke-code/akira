package com.zekecode.akira_financialtracker.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AkiraPrefs", MODE_PRIVATE)
        val isSetupComplete = sharedPreferences.getBoolean("IsSetupComplete", false)

        if(!isSetupComplete) {
            val intent = Intent(this, FirstSetupActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}