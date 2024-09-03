package com.zekecode.akira_financialtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.FragmentSettingsBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.SettingsViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Observe LiveData from ViewModel
        viewModel.username.observe(viewLifecycleOwner) { username ->
            binding.tvName.text = getString(R.string.settings_username, username)
            binding.tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_text))
        }

        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            binding.tvBudget.text = getString(R.string.settings_budget, budget)
            binding.tvBudget.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_text))
        }

        viewModel.notificationsEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.tvNotifications.text = getString(R.string.settings_notification_status, if (enabled) "yes" else "no")
            binding.tvNotifications.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_text))
        }

        // Set up click listeners to enable users to modify username, budget, and notifications permissions
        binding.rlNameSetting.setOnClickListener {
            showInputDialog("Change Username", viewModel.username.value ?: "Username") { newUsername ->
                viewModel.updateUsername(newUsername)
            }
        }

        binding.rlBudgetSetting.setOnClickListener {
            showInputDialog("Change Monthly Budget", viewModel.budget.value.toString() ?: "0.0") { newBudget ->
                viewModel.updateBudget(newBudget)
            }
        }

        return binding.root
    }

    private fun showInputDialog(title: String, currentValue: String, onSave: (String) -> Unit) {
        val editText = EditText(requireContext())
        editText.setText(currentValue)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newValue = editText.text.toString()
                onSave(newValue)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
