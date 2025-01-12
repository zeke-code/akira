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

    companion object {
        const val REQUEST_INSTALL_PERMISSION = 1001
    }

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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INSTALL_PERMISSION && resultCode == RESULT_OK) {
            viewModel.downloadAndInstallApk(this, "zeke-code", "akira")
        }
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
                viewModel.downloadAndInstallApk(this, "zeke-code", "akira")
            }
            .setNegativeButton("Later", null)
            .show()
    }
}