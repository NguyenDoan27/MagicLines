package com.example.magiclines.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.magiclines.R
import com.example.magiclines.databinding.MusicItemBinding
import com.example.magiclines.interfaces.IOnClick
import com.example.magiclines.models.Audio

class MusicAdapter(private val context: Context,
                   private val music: List<Audio>,
                   private var currentMusicPosition: Int,
                   private val click: IOnClick):
    RecyclerView.Adapter<MusicAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(MusicItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicAdapter.ViewHolder, position: Int) {
        holder.name.text = music[position].getName()
        (holder.itemView.background as GradientDrawable).apply {
            if (position == currentMusicPosition) {
                setStroke(3, ContextCompat.getColor(context, R.color.white))
            } else {
                setStroke(0, Color.TRANSPARENT)
            }
        }

        holder.itemView.setOnClickListener {
            click.onclick(position)
            setSelectedMusic(position)
        }
    }

    override fun getItemCount(): Int  = music.size

    class ViewHolder(binding: MusicItemBinding): RecyclerView.ViewHolder(binding.root) {
        val name = binding.tvNameMusic
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setSelectedMusic(position: Int){
        currentMusicPosition = position
        notifyDataSetChanged()
    }
}