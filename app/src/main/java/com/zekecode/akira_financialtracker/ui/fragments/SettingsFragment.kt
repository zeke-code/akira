package com.zekecode.akira_financialtracker.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.DialogConfirmationBinding
import com.zekecode.akira_financialtracker.databinding.DialogInputBinding
import com.zekecode.akira_financialtracker.databinding.DialogSpinnerInputBinding
import com.zekecode.akira_financialtracker.databinding.FragmentSettingsBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

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

        setupUI()
        setupClickListeners()

        return binding.root
    }

    private fun setupUI() {
        viewModel.appVersion.observe(viewLifecycleOwner) { appVersion ->
            binding.tvAppVersion.text = getString(R.string.settings_app_version, appVersion)
        }

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
                getString(R.string.settings_api_key_viewer, "not set")
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
        binding.llNameSetting.setOnClickListener {
            showInputDialog("Change Username", viewModel.username.value ?: "Username", { newUsername ->
                viewModel.updateUsername(newUsername)
            }, isNumeric = false)
        }

        binding.llBudgetSetting.setOnClickListener {
            showInputDialog("Change Monthly Budget", viewModel.budget.value.toString(), { newBudget ->
                viewModel.updateBudget(newBudget)
            }, isNumeric = true)
        }

        binding.llCurrencySetting.setOnClickListener {
            showCurrencySelectionDialog()
        }

        binding.llNotificationsSetting.setOnClickListener {
            handleNotificationToggle()
        }

        binding.llApiKeySetter.setOnClickListener {
            showInputDialog("Set your API key", viewModel.apiKey.value ?: "", { newApiKey ->
                viewModel.updateApiKey(newApiKey)
            })
        }

        binding.llDeleteAllTransactions.setOnClickListener {
            showDeleteAllTransactionsDialog()
        }

        binding.llAppVersion.setOnClickListener {
            openGitHubReleasesPage()
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
        val dialogBinding = DialogConfirmationBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvConfirmationDialogTitle.text = getString(R.string.settings_disable_notifications_dialog_title)
        dialogBinding.tvConfirmationDialogDescription.text = getString(R.string.settings_disable_notifications_dialog_description)
        dialogBinding.btnSave.text = getString(R.string.dialog_go_to_settings)
        dialogBinding.btnCancel.text = getString(R.string.dialog_cancel_button)

        dialogBinding.btnSave.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun showPermissionDeniedDialog() {
        val dialogBinding = DialogConfirmationBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvConfirmationDialogTitle.text = getString(R.string.settings_notification_permission_denied_title)
        dialogBinding.tvConfirmationDialogDescription.text = getString(R.string.settings_notification_permission_denied_message)
        dialogBinding.btnSave.text = getString(R.string.dialog_go_to_settings)
        dialogBinding.btnCancel.text = getString(R.string.dialog_cancel_button)

        dialogBinding.btnSave.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

        dialogBinding.tvConfirmationDialogTitle.text = getString(R.string.dialog_delete_all_transactions_title)
        dialogBinding.tvConfirmationDialogDescription.text = getString(R.string.dialog_delete_all_transactions_description)

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

    private val openBrowserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // We can handle action when browser is opened if needed, but we don't at the moment.
        // Maybe in the future, idk
    }

    private fun openGitHubReleasesPage() {
        val releasesUrl = "https://github.com/zeke-code/akira/releases"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releasesUrl))

        try {
            openBrowserLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No browser found to open the link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
