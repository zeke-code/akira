package com.zekecode.akira_financialtracker.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zekecode.akira_financialtracker.databinding.ActivityFirstSetupBinding

class FirstSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstSetupBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirstSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AkiraPrefs", MODE_PRIVATE)

        binding.saveButton.setOnClickListener {
            val userName = binding.userNameEditText.text.toString().trim()
            val monthlyBudgetStr = binding.monthlyBudgetEditText.text.toString().trim()

            if (userName.isNotEmpty() && monthlyBudgetStr.isNotEmpty()) {
                val monthlyBudget = monthlyBudgetStr.toInt()

                with(sharedPreferences.edit()) {
                    putString("UserName", userName)
                    putInt("MonthlyBudget", monthlyBudget)
                    apply()
                }

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
