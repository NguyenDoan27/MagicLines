package com.example.magiclines.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt
import com.example.magiclines.databinding.BackgroundItemBinding
import com.example.magiclines.interfaces.IOnClick
import com.example.magiclines.models.Color


class BackgroundAdapter(private val colors: List<Color>,
                        private var currentColor: Int,
                        private val click: IOnClick):
    RecyclerView.Adapter<BackgroundAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(BackgroundItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val drawable = holder.view.background as GradientDrawable
        drawable.setColor(colors[position].getCodeColor()!!.toColorInt())
        if (position == currentColor){
            drawable.setStroke(3, android.graphics.Color.YELLOW)
        }else{
            drawable.setStroke(0, android.graphics.Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            click.onclick(position)
            setSelectedColor(position)
        }
    }

    override fun getItemCount(): Int = colors.size

    class ViewHolder(view: BackgroundItemBinding): RecyclerView.ViewHolder(view.root){
        val view: View = view.viewBackground
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedColor(position: Int) {
        currentColor = position
        notifyDataSetChanged()
    }
}