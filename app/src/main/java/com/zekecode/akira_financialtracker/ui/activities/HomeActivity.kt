package com.zekecode.akira_financialtracker.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zekecode.akira_financialtracker.R
import android.content.SharedPreferences
import com.zekecode.akira_financialtracker.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding;
    private lateinit var sharedPreferences: SharedPreferences;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AkiraPrefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("UserName", "User")
        val monthlyBudget = sharedPreferences.getInt("MonthlyBudget", 0)

        binding.welcomeTextView.text = getString(R.string.home_welcome_text, userName)
        binding.budgetTextView.text = getString(R.string.home_budget_text, monthlyBudget)
    }
}