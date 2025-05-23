package com.example.magiclines.common.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.magiclines.R
import com.example.magiclines.databinding.MusicItemBinding
import com.example.magiclines.models.Audio
import java.util.concurrent.Executors

class MusicAdapter2(private val context: Context, var currentPosition: Int, val onItemClick: (Int) -> Unit):
    ListAdapter<Audio, MusicAdapter2.ViewHolder>(
        AsyncDifferConfig.Builder<Audio>(AudioCallback())
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor()).build()){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(MusicItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = getItem(position).name
        (holder.itemView.background as GradientDrawable).apply {
            if (position == currentPosition) {
                setStroke(3, ContextCompat.getColor(context, R.color.white))
            } else {
                setStroke(0, Color.TRANSPARENT)
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(position)
            setSelectedMusic(position)
        }
    }

    class ViewHolder(binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root) {
        val name = binding.tvNameMusic
    }

    @SuppressLint("NotifyDataSetChanged")
     fun setSelectedMusic(position: Int){
        currentPosition = position
        notifyDataSetChanged()
    }
}

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