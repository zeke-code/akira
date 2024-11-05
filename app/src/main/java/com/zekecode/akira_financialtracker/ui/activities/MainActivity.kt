package com.zekecode.akira_financialtracker.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zekecode.akira_financialtracker.databinding.ActivityMainBinding
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.data.local.repository.SharedPreferencesRepository
import com.zekecode.akira_financialtracker.notifiers.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    @Inject
    lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Register for permission result callback
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, show the daily reminder
            appNotificationManager.showDailyLoggingReminder()
        } else {
            // Permission denied, handle it appropriately (e.g., inform the user)
            handlePermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and handle notification permission
        checkAndRequestNotificationPermission()

        // Set up navigation
        setupNavigation()

        // Check if the initial setup is complete
        if (!sharedPreferencesRepository.isSetupComplete()) {
            startActivity(Intent(this, FirstSetupActivity::class.java))
            finish()
            return
        }
    }

    // Helper function to set up navigation
    private fun setupNavigation() {
        val navHostFragment = binding.navHostFragment.getFragment<NavHostFragment>()
        navController = navHostFragment.navController

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }

    // Helper function to check notification permission and request if needed
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted, show daily reminder
                appNotificationManager.showDailyLoggingReminder()
            } else {
                // Request the notification permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // No permission needed for Android versions below 13
            appNotificationManager.showDailyLoggingReminder()
        }
    }

    // Handle what happens if the notification permission is denied
    private fun handlePermissionDenied() {
        // Optionally show a message or update UI to inform the user that notifications are disabled
    }
}
