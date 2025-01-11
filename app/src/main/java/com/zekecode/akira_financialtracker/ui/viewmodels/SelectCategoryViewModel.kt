package com.zekecode.akira_financialtracker.ui.viewmodels

import androidx.lifecycle.*
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectCategoryViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    val allCategories: LiveData<List<CategoryModel>> = repository.getAllCategories()

    private val _selectedCategory = MutableLiveData<CategoryModel?>()
    val selectedCategory: LiveData<CategoryModel?> get() = _selectedCategory

    fun setSelectedCategory(category: CategoryModel) {
        _selectedCategory.value = category
    }
}