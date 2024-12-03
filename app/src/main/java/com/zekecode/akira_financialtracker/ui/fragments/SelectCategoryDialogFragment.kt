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
    private lateinit var adapter: CategoryAdapter

    private var selectedCategory: CategoryModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectCategoryBinding.inflate(inflater, container, false)

        setupAdapter()
        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupAdapter() {
        adapter = CategoryAdapter { category ->
            selectedCategory = category
            adapter.setSelectedCategory(category.id)
        }
        binding.rvCategoryList.layoutManager = LinearLayoutManager(context)
        binding.rvCategoryList.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)

            // If a category was previously selected, reflect that in the adapter
            val previouslySelectedCategory = viewModel.selectedCategory.value
            if (previouslySelectedCategory != null) {
                adapter.setSelectedCategory(previouslySelectedCategory.id)
                selectedCategory = previouslySelectedCategory
            }
        }
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            if (selectedCategory != null) {
                viewModel.setSelectedCategory(selectedCategory!!)
                dismiss()
            } else {
                Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}