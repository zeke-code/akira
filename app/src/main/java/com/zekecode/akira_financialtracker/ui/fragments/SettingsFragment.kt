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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.entities.SettingItem
import com.zekecode.akira_financialtracker.databinding.DialogConfirmationBinding
import com.zekecode.akira_financialtracker.databinding.DialogInputBinding
import com.zekecode.akira_financialtracker.databinding.DialogSpinnerInputBinding
import com.zekecode.akira_financialtracker.databinding.FragmentSettingsBinding
import com.zekecode.akira_financialtracker.ui.adapters.SettingsAdapter
import com.zekecode.akira_financialtracker.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateNotificationsEnabled(isGranted)
        if (!isGranted) showPermissionDeniedDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observeViewModel()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = SettingsAdapter(getSettingsItems())

        val dividerItemDecoration = DividerItemDecoration(binding.recyclerView.context, LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun observeViewModel() {
        viewModel.apply {
            username.observe(viewLifecycleOwner) { updateRecyclerView() }
            budget.observe(viewLifecycleOwner) { updateRecyclerView() }
            selectedCurrency.observe(viewLifecycleOwner) { updateRecyclerView() }
            notificationsEnabled.observe(viewLifecycleOwner) { updateRecyclerView() }
            apiKey.observe(viewLifecycleOwner) { updateRecyclerView() }
            appVersion.observe(viewLifecycleOwner) { updateRecyclerView() }
        }
    }

    private fun updateRecyclerView() {
        (binding.recyclerView.adapter as? SettingsAdapter)?.updateItems(getSettingsItems())
    }

    private fun getSettingsItems(): List<SettingItem> {
        return listOf(
            SettingItem(
                iconResId = R.drawable.ic_user,
                title = getString(R.string.settings_username),
                subtitle = viewModel.username.value,
                onClickAction = { showInputDialog("Change Username", viewModel.username.value.orEmpty()) { viewModel.updateUsername(it) } }
            ),
            SettingItem(
                iconResId = R.drawable.ic_dollar,
                title = getString(R.string.settings_budget),
                subtitle = viewModel.budget.value.toString(),
                onClickAction = { showInputDialog("Change Monthly Budget", viewModel.budget.value.toString(), isNumeric = true) { viewModel.updateBudget(it) } }
            ),
            SettingItem(
                iconResId = R.drawable.ic_currency_exchange,
                title = getString(R.string.settings_current_currency),
                subtitle = viewModel.selectedCurrency.value,
                onClickAction = { showCurrencySelectionDialog() }
            ),
            SettingItem(
                iconResId = R.drawable.ic_api,
                title = getString(R.string.settings_api_key),
                subtitle = getString(
                    R.string.settings_api_key_status_description,
                    if (viewModel.apiKey.value.isNullOrEmpty()) "Unset" else "Set"
                ),
                onClickAction = { showInputDialog("Set API Key", viewModel.apiKey.value.orEmpty()) { viewModel.updateApiKey(it) } }
            ),
            SettingItem(
                iconResId = R.drawable.ic_notifications,
                title = getString(R.string.settings_notification_status),
                subtitle = getString(R.string.settings_notification_status_description, if (viewModel.notificationsEnabled.value == true) "On" else "Off"),
                onClickAction = { toggleNotifications() }
            ),
            SettingItem(
                iconResId = R.drawable.ic_delete_bin,
                title = getString(R.string.settings_delete_all_transactions),
                subtitle = getString(R.string.settings_delete_all_transactions_description),
                onClickAction = { showConfirmationDialog("Delete All Transactions", "Are you sure you want to delete all transactions?") { viewModel.deleteAllTransactions() } }
            ),
            SettingItem(
                iconResId = R.drawable.ic_info,
                title = getString(R.string.settings_app_version),
                subtitle = viewModel.appVersion.value,
                onClickAction = { openGitHubReleasesPage() }
            )
        )
    }

    private fun showInputDialog(
        title: String,
        currentValue: String,
        isNumeric: Boolean = false,
        onSave: (String) -> Unit
    ) {
        val binding = DialogInputBinding.inflate(LayoutInflater.from(requireContext()))
        binding.apply {
            tvDialogTitle.text = title
            etInput.setText(currentValue)
            if (isNumeric) etInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
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

    private fun showCurrencySelectionDialog() {
        val binding = DialogSpinnerInputBinding.inflate(LayoutInflater.from(requireContext()))
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.currency_options)
        )
        binding.currencySpinner.adapter = adapter

        AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(binding.root)
            .create()
            .apply {
                binding.btnSave.setOnClickListener {
                    viewModel.updateCurrency(binding.currencySpinner.selectedItem.toString())
                    dismiss()
                }
                binding.btnCancel.setOnClickListener { dismiss() }
            }
            .show()
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val binding = DialogConfirmationBinding.inflate(LayoutInflater.from(requireContext()))
        binding.apply {
            tvConfirmationDialogTitle.text = title
            tvConfirmationDialogDescription.text = message
        }

        AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(binding.root)
            .create()
            .apply {
                binding.btnSave.setOnClickListener {
                    onConfirm()
                    dismiss()
                }
                binding.btnCancel.setOnClickListener { dismiss() }
            }
            .show()
    }

    private fun toggleNotifications() {
        val notificationsEnabled = viewModel.notificationsEnabled.value ?: false

        if (notificationsEnabled) {
            showConfirmationDialog(
                "Disable Notifications",
                "Are you sure you want to disable notifications?"
            ) {
                viewModel.updateNotificationsEnabled(false)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.updateNotificationsEnabled(true)
                }
            } else {
                viewModel.updateNotificationsEnabled(true)
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val binding = DialogConfirmationBinding.inflate(LayoutInflater.from(requireContext()))
        binding.apply {
            tvConfirmationDialogTitle.text = getString(R.string.settings_notification_permission_denied_title)
            tvConfirmationDialogDescription.text = getString(R.string.settings_notification_permission_denied_message)
        }

        AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(binding.root)
            .create()
            .apply {
                binding.btnSave.setOnClickListener {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireActivity().packageName, null)
                    }
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Unable to open settings.", Toast.LENGTH_SHORT).show()
                    }
                    dismiss()
                }
                binding.btnCancel.setOnClickListener { dismiss() }
            }
            .show()
    }

    private fun openGitHubReleasesPage() {
        val url = "https://github.com/zeke-code/akira/releases"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
