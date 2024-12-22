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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.DialogConfirmationBinding
import com.zekecode.akira_financialtracker.databinding.DialogInputBinding
import com.zekecode.akira_financialtracker.databinding.DialogSpinnerInputBinding
import com.zekecode.akira_financialtracker.databinding.FragmentSettingsBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
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

        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupObservers() {
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

        viewModel.notificationsEnabled.observe(viewLifecycleOwner) {
            checkSystemNotificationPermission()
        }

        viewModel.apiKey.observe(viewLifecycleOwner) { apiKey ->
            val displayText = if (apiKey.isNullOrEmpty()) {
                getString(R.string.settings_api_key_viewer, "none")
            } else {
                getString(R.string.settings_api_key_viewer, "set")
            }
            binding.tvApiKey.text = displayText
        }

        viewModel.invalidInputToastText.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
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

        binding.rlDeleteAllTransactions.setOnClickListener {
            showDeleteAllTransactionsDialog()
        }
    }

    private fun showInputDialog(title: String, currentValue: String, onSave: (String) -> Unit, isNumeric: Boolean = false) {
        val binding = DialogInputBinding.inflate(LayoutInflater.from(requireContext())).apply {
            tvDialogTitle.text = title
            etInput.setText(currentValue)

            if (isNumeric) {
                etInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }

        AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(binding.root)
            .create()
            .apply {
                binding.btnSave.setOnClickListener {
                    onSave(binding.etInput.text.toString())
                    dismiss()
                }
                binding.btnCancel.setOnClickListener { dismiss() }
            }
            .show()
    }


    private fun handleNotificationToggle() {
        val notificationsEnabled = viewModel.notificationsEnabled.value ?: false

        if (notificationsEnabled) {
            showDisableNotificationsDialog()
        } else {
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

        viewModel.updateNotificationsEnabled(notificationsEnabled)
        binding.tvNotifications.text = getString(R.string.settings_notification_status, if (notificationsEnabled) "on" else "off")
    }

    private fun showDisableNotificationsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_disable_notifications_dialog_title)
            .setMessage(R.string.settings_disable_notifications_dialog_description)
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
        val binding = DialogSpinnerInputBinding.inflate(LayoutInflater.from(requireContext()))
        val currencyOptions = resources.getStringArray(R.array.currency_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(binding.root)
            .create()

        binding.btnSave.setOnClickListener {
            val selectedCurrency = binding.currencySpinner.selectedItem.toString()
            viewModel.updateCurrency(selectedCurrency)
            dialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteAllTransactionsDialog() {
        val dialogBinding = DialogConfirmationBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            viewModel.deleteAllTransactions()
            Toast.makeText(
                requireContext(),
                R.string.transactions_deleted_success,
                Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
