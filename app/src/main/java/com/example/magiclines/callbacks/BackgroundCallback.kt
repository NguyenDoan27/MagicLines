package com.example.magiclines.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.magiclines.models.Color

class BackgroundCallback: DiffUtil.ItemCallback<Color>() {
    override fun areItemsTheSame(
        oldItem: Color,
        newItem: Color
    ): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(
        oldItem: Color,
        newItem: Color
    ): Boolean {
        return oldItem == newItem
    }
}