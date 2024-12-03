package com.zekecode.akira_financialtracker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onItemClicked: (CategoryModel) -> Unit
) : ListAdapter<CategoryModel, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    private var selectedCategoryId: Int? = null

    fun setSelectedCategory(categoryId: Int?) {
        val previousSelectedPosition = currentList.indexOfFirst { it.id == selectedCategoryId }
        val newSelectedPosition = currentList.indexOfFirst { it.id == categoryId }
        selectedCategoryId = categoryId

        // Refresh the previous and new selected items
        if (previousSelectedPosition >= 0) {
            notifyItemChanged(previousSelectedPosition)
        }
        if (newSelectedPosition >= 0) {
            notifyItemChanged(newSelectedPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        val isSelected = category.id == selectedCategoryId
        holder.bind(category, isSelected)
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryModel, isSelected: Boolean) {
            binding.ivCategoryIcon.setImageResource(category.icon)
            binding.tvSelectedCategory.text = category.name

            val cardView = binding.root
            cardView.isChecked = isSelected

            binding.root.setOnClickListener {
                onItemClicked(category)
                setSelectedCategory(category.id)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoryModel>() {
            override fun areItemsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
