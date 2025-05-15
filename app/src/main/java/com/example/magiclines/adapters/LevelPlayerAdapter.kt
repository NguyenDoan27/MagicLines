package com.example.magiclines.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.magiclines.R
import com.example.magiclines.databinding.LevelItemBinding
import com.example.magiclines.interfaces.IOnClick
import com.example.magiclines.models.Level

class LevelPlayerAdapter(private val context: Context, private var levels: List<Level>,private val click: IOnClick):
    RecyclerView.Adapter<LevelPlayerAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(LevelItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = levels[position]
        if (item.getIsComplete() == true){
            holder.imgItemLevel.setImageResource(item.getResourceId()!!)
            holder.imgItemLevel.visibility = View.VISIBLE
            holder.tvNumLevel.visibility = View.GONE
            holder.imgRightStar.visibility = View.VISIBLE
            holder.imgCenterStar.visibility = View.VISIBLE
            holder.imgLeftStar.visibility = View.VISIBLE
            Log.e("TAG", "onBindViewHolder: ${item.getStar()} ${item.getNumLevel()}", )
            when(item.getStar()){
                0 -> {
                    holder.imgRightStar.setImageResource(R.drawable.empty_star)
                    holder.imgCenterStar.setImageResource(R.drawable.empty_star)
                    holder.imgLeftStar.setImageResource(R.drawable.empty_star)
                }
                1 -> {
                    holder.imgLeftStar.setImageResource(R.drawable.empty_star)
                    holder.imgRightStar.setImageResource(R.drawable.empty_star)
                }
                2 -> {
                    holder.imgRightStar.setImageResource(R.drawable.empty_star)
                }
            }
        }else{
            holder.tvNumLevel.text = item.getNumLevel().toString()
            holder.imgItemLevel.visibility = View.GONE
            holder.tvNumLevel.visibility = View.VISIBLE
            holder.imgRightStar.visibility = View.GONE
            holder.imgCenterStar.visibility = View.GONE
            holder.imgLeftStar.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            click.onclick(position)
        }
    }

    override fun getItemCount(): Int = levels.size

    class ViewHolder(binding: LevelItemBinding): RecyclerView.ViewHolder(binding.root) {
        val imgItemLevel = binding.imgItemLevel
        val tvNumLevel = binding.tvNumLevel
        val imgLeftStar = binding.imgLeftStar
        val imgCenterStar = binding.imgCenterStar
        val imgRightStar = binding.imgRightStar
    }
}