package com.example.magiclines.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.magiclines.databinding.CategoryItemBinding

class CategoryAdapter(val context: Context,
                      private var currentCategory: Int,
                      val categories: List<String>, val listener: (String, Int) -> Unit) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvName = binding.tvName
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvName.text = category
        if (position == currentCategory) {
            holder.itemView.isEnabled = false
        }else{
            holder.itemView.isEnabled = true
        }
        holder.itemView.setOnClickListener {
            listener(category, position)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun setCurrentCategory(position: Int) {
        currentCategory = position
        notifyDataSetChanged()
    }
}