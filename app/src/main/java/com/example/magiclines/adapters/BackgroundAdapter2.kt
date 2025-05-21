package com.example.magiclines.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.magiclines.callbacks.BackgroundCallback
import com.example.magiclines.databinding.BackgroundItemBinding
import com.example.magiclines.models.Color
import java.util.concurrent.Executors

class BackgroundAdapter2(private val context: Context,
                         private var currentPosition: Int,
                         private val onItemClick: (Int) -> (Unit)):
    ListAdapter<Color, BackgroundAdapter2.ViewHolder>(AsyncDifferConfig.Builder<Color>(BackgroundCallback())
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor()).build()){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BackgroundAdapter2.ViewHolder {
        return ViewHolder(BackgroundItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BackgroundAdapter2.ViewHolder, position: Int) {
        val drawable = holder.view.background as GradientDrawable
        drawable.setColor(getItem(position).codeColor.toColorInt())
        if (position == currentPosition){
            drawable.setStroke(3, android.graphics.Color.YELLOW)
        }else{
            drawable.setStroke(0, android.graphics.Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            onItemClick(position)
            setSelectedColor(position)
        }
    }

    class ViewHolder(view: BackgroundItemBinding): RecyclerView.ViewHolder(view.root){
        val view: View = view.viewBackground
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedColor(position: Int) {
        currentPosition = position
        notifyDataSetChanged()
    }

}