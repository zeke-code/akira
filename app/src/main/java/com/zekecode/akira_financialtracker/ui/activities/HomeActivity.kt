package com.zekecode.akira_financialtracker.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.zekecode.akira_financialtracker.R
import android.content.SharedPreferences
import androidx.activity.viewModels
import com.zekecode.akira_financialtracker.Application
import com.zekecode.akira_financialtracker.databinding.ActivityHomeBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModel
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding;
    private lateinit var sharedPreferences: SharedPreferences;

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as Application).repository, sharedPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedPreferences = getSharedPreferences("AkiraPrefs", MODE_PRIVATE)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.userName.observe(this) { userName ->
            binding.welcomeTextView.text = getString(R.string.home_welcome_text, userName)
        }

        viewModel.monthlyBudget.observe(this) { monthlyBudget ->
            binding.budgetTextView.text = getString(R.string.home_budget_text, monthlyBudget)
        }

    }
}