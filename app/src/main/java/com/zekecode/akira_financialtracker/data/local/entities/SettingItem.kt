package com.zekecode.akira_financialtracker.data.local.entities

data class SettingItem(
    val iconResId: Int,
    val title: String,
    val subtitle: String? = null,
    val onClickAction: (() -> Unit)? = null
)
