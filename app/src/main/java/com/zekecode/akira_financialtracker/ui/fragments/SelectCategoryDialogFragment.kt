package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.databinding.DialogSelectCategoryBinding
import com.zekecode.akira_financialtracker.ui.adapters.CategoryAdapter
import com.zekecode.akira_financialtracker.ui.viewmodels.CreateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectCategoryDialogFragment : DialogFragment() {

    private var _binding: DialogSelectCategoryBinding? = null
    private val binding get() = _binding!!

    // Use Hilt to inject the ViewModel
    private val viewModel: CreateViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectCategoryBinding.inflate(inflater, container, false)

        // Setup RecyclerView for categories
        binding.rvCategoryList.layoutManager = LinearLayoutManager(context)
        val adapter = CategoryAdapter { category ->
            viewModel.setSelectedCategory(category)
            onCategorySelected(category)
        }
        binding.rvCategoryList.adapter = adapter

        // Observe categories from ViewModel
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
        }

        // Set up buttons
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun onCategorySelected(category: CategoryModel) {
        Toast.makeText(requireContext(), "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
