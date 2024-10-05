package com.zekecode.akira_financialtracker.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.FragmentSettingsBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.SettingsViewModel
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    // Register permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.updateNotificationsEnabled(true)
        } else {
            viewModel.updateNotificationsEnabled(false)
            showPermissionDeniedDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        viewModel.username.observe(viewLifecycleOwner) { username ->
            val fullText = getString(R.string.settings_username, username)
            binding.tvName.text = fullText
        }

        viewModel.combinedBudgetText.observe(viewLifecycleOwner) { fullText ->
            binding.tvBudget.text = fullText
            binding.tvBudget.invalidate()
        }

        viewModel.selectedCurrency.observe(viewLifecycleOwner) { selectedCurrency ->
            val fullText = getString(R.string.settings_current_currency, selectedCurrency)
            binding.tvCurrency.text = fullText
        }

        // Observe system and app-level notification settings
        viewModel.notificationsEnabled.observe(viewLifecycleOwner) {
            checkSystemNotificationPermission()
        }


        viewModel.apiKey.observe(viewLifecycleOwner) { apiKey ->
            val displayText = if (apiKey.isNullOrEmpty()) {
                getString(R.string.settings_api_key_viewer, "none")
            } else {
                getString(R.string.settings_api_key_viewer, apiKey)
            }
            binding.tvApiKey.text = displayText
        }

        // Set up click listeners to enable users to modify username, budget, and notifications permissions
        binding.rlNameSetting.setOnClickListener {
            showInputDialog("Change Username", viewModel.username.value ?: "Username", { newUsername ->
                viewModel.updateUsername(newUsername)
            }, isNumeric = false)
        }

        binding.rlBudgetSetting.setOnClickListener {
            showInputDialog("Change Monthly Budget", viewModel.budget.value.toString(), { newBudget ->
                viewModel.updateBudget(newBudget)
            }, isNumeric = true)
        }

        binding.rlCurrencySetting.setOnClickListener {
            showCurrencySelectionDialog()
        }

        binding.rlNotificationsSetting.setOnClickListener {
            handleNotificationToggle()
        }

        binding.rlApiKeySetter.setOnClickListener {
            showInputDialog("Set your API key", viewModel.apiKey.value ?: "", { newApiKey ->
                viewModel.updateApiKey(newApiKey)
            })
        }

        return binding.root
    }

    private fun showInputDialog(title: String, currentValue: String, onSave: (String) -> Unit, isNumeric: Boolean = false) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.etInput)
        val titleTextView = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        titleTextView.text = title
        editText.setText(currentValue)

        if (isNumeric) {
            editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val newValue = editText.text.toString()
            onSave(newValue)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleNotificationToggle() {
        // Check if notifications are currently enabled in your app settings
        val notificationsEnabled = viewModel.notificationsEnabled.value ?: false

        if (notificationsEnabled) {
            // If enabled, show dialog to guide the user to app settings to disable
            showDisableNotificationsDialog()
        } else {
            // If not enabled, check if permission is needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestNotificationPermission()
                } else {
                    viewModel.updateNotificationsEnabled(true)
                }
            } else {
                viewModel.updateNotificationsEnabled(true)
            }
        }
    }

    private fun checkSystemNotificationPermission() {
        val notificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // Update SharedPreferences and ViewModel based on the system permission status
        viewModel.updateNotificationsEnabled(notificationsEnabled)
        // Update UI based on the current system permission status
        binding.tvNotifications.text = getString(R.string.settings_notification_status, if (notificationsEnabled) "on" else "off")
    }

    private fun showDisableNotificationsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Disable Notifications")
            .setMessage("To disable notifications, please go to the app settings and turn them off.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission Denied")
            .setMessage("To enable notifications, please allow the permission in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCurrencySelectionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_spinner_input, null)
        val spinnerCurrency = dialogView.findViewById<Spinner>(R.id.currencySpinner)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Set up the Spinner
        val currencyOptions = resources.getStringArray(R.array.currency_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val selectedCurrency = spinnerCurrency.selectedItem.toString()
            viewModel.updateCurrency(selectedCurrency)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
