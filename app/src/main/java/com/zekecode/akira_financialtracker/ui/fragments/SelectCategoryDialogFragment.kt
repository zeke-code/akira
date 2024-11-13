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

        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupObservers() {
        // Observe categories from ViewModel
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            (binding.rvCategoryList.adapter as? CategoryAdapter)?.submitList(categories)
        }
    }

    private fun setupListeners() {
        // Set up RecyclerView and adapter for categories
        binding.rvCategoryList.layoutManager = LinearLayoutManager(context)
        val adapter = CategoryAdapter { category ->
            viewModel.setSelectedCategory(category)
        }
        binding.rvCategoryList.adapter = adapter

        // Cancel button listener
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // Save button listener
        binding.btnSave.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}