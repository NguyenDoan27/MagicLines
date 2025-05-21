package com.example.magiclines.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.magiclines.models.Audio

class AudioCallback: DiffUtil.ItemCallback<Audio>() {
    override fun areItemsTheSame(
        oldItem: Audio,
        newItem: Audio
    ): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(
        oldItem: Audio,
        newItem: Audio
    ): Boolean {
        return oldItem == newItem
    }
}