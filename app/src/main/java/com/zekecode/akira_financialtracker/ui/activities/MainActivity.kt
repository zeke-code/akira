package com.zekecode.akira_financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.ActivityMainBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.checkAndUpdateBudget()

        if (!viewModel.isSetupComplete()) {
            startActivity(Intent(this, FirstSetupActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()

        setUpObservers()
    }

    private fun setupNavigation() {
        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        navController = navHostFragment.navController

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }

    private fun setUpObservers() {
        viewModel.isUpdateAvailable.observe(this) { isUpdateAvailable ->
            if (isUpdateAvailable) {
                showUpdateDialog()
            }
        }
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(this, R.style.CustomDialogUpdate)
            .setTitle("Update Available")
            .setMessage("A new version of the app is available. Would you like to update now?")
            .setPositiveButton("Update") { _, _ ->
                downloadAndUpdate()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun downloadAndUpdate() {
        // Implement the logic to download the APK and prompt the user to install it
        // This part requires handling file downloads and initiating the installation intent
    }
}