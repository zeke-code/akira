package com.zekecode.akira_financialtracker.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AkiraPrefs", MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("IsFirstLaunch", true)

        if (isFirstLaunch) {
            with(sharedPreferences.edit()) {
                putBoolean("IsFirstLaunch", false)
                apply()
            }
            val intent = Intent(this, FirstSetupActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
