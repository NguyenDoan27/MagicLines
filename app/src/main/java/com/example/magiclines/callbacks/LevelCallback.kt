package com.example.magiclines.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.magiclines.models.Level

class LevelCallback: DiffUtil.ItemCallback<Level>() {
    override fun areItemsTheSame(
        oldItem: Level,
        newItem: Level
    ): Boolean {
        return oldItem.numLevel == newItem.numLevel
    }

    override fun areContentsTheSame(
        oldItem: Level,
        newItem: Level
    ): Boolean {
        return oldItem == newItem
    }
}