package com.example.magiclines.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.magiclines.R
import com.example.magiclines.databinding.LevelItemBinding
import com.example.magiclines.models.Level
import java.util.concurrent.Executors

class LevelPlayerAdapter2 (val context: Context, val listener: FilterListener, val onItemClick: (Int) -> Unit):
    ListAdapter<Level, LevelPlayerAdapter2.ViewHolder>(
        AsyncDifferConfig.Builder<Level>(LevelCallback())
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor()).build()), Filterable {
            private var originalItems = emptyList<Level>()
            private var filteredItems = emptyList<Level>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(LevelItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item.isComplete == true){
            holder.imgItemLevel.setImageResource(item.resourceId)
            holder.imgItemLevel.visibility = View.VISIBLE
            holder.tvNumLevel.visibility = View.GONE
            holder.imgRightStar.visibility = View.VISIBLE
            holder.imgCenterStar.visibility = View.VISIBLE
            holder.imgLeftStar.visibility = View.VISIBLE

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
            holder.tvNumLevel.text = item.numLevel.toString()
            holder.imgItemLevel.visibility = View.GONE
            holder.tvNumLevel.visibility = View.VISIBLE
            holder.imgRightStar.visibility = View.GONE
            holder.imgCenterStar.visibility = View.GONE
            holder.imgLeftStar.visibility = View.GONE
        }
    }

    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val query = constraint.toString().lowercase()

                results.values = if (query.isBlank()) {
                    originalItems
                } else {
                    originalItems.filter { it.category.lowercase().contains(query) }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as List<Level>
                listener.onFilterApplied(filteredItems)
                submitList(filteredItems)
            }
        }
    }


    inner class ViewHolder(binding: LevelItemBinding): RecyclerView.ViewHolder(binding.root) {
        val imgItemLevel = binding.imgItemLevel
        val tvNumLevel = binding.tvNumLevel
        val imgLeftStar = binding.imgLeftStar
        val imgCenterStar = binding.imgCenterStar
        val imgRightStar = binding.imgRightStar
        init {
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    fun setItems(items: List<Level>) {
        originalItems = items
        filteredItems = items
        submitList(filteredItems)
    }

    fun getItemsFiltered(): List<Level> {
        return filteredItems
    }

    fun setOriginalItems() {
        submitList(originalItems)
        filteredItems = originalItems
    }

    interface FilterListener {
        fun onFilterApplied(filteredList: List<Level>)
    }

}

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