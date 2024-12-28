package com.zekecode.akira_financialtracker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zekecode.akira_financialtracker.data.local.entities.SettingItem
import com.zekecode.akira_financialtracker.databinding.ItemSettingBinding

class SettingsAdapter(
    private var items: List<SettingItem>
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem) {
            binding.ivIcon.setImageResource(item.iconResId)
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle.orEmpty()
            binding.tvSubtitle.visibility = if (item.subtitle.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.root.setOnClickListener { item.onClickAction?.invoke() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    /**
     * Update the items in the adapter and notify the change.
     */
    fun updateItems(newItems: List<SettingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
