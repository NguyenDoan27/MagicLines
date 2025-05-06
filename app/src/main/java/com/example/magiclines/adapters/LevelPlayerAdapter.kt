package com.example.magiclines.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.magiclines.databinding.LevelItemBinding
import com.example.magiclines.interfaces.IOnClick
import com.example.magiclines.models.Level

class LevelPlayerAdapter(private val context: Context, private var levels: List<Level>,private val click: IOnClick):
    RecyclerView.Adapter<LevelPlayerAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LevelPlayerAdapter.ViewHolder {
        return ViewHolder(LevelItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: LevelPlayerAdapter.ViewHolder, position: Int) {
        val item = levels[position]
        if (item.getIsComplete() == true){
            holder.imgItemLevel.setImageResource(item.getResourceId()!!)
            holder.imgItemLevel.visibility = View.VISIBLE
            holder.tvNumLevel.visibility = View.GONE
        }else{
            holder.tvNumLevel.text = item.getNumLevel().toString()
            holder.imgItemLevel.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            click.onclick(position)
        }
    }

    override fun getItemCount(): Int = levels.size

    class ViewHolder(binding: LevelItemBinding): RecyclerView.ViewHolder(binding.root) {
        val imgItemLevel = binding.imgItemLevel
        val tvNumLevel = binding.tvNumLevel
    }
}