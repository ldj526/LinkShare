package com.example.linkshare.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R

class PopularSearchAdapter : RecyclerView.Adapter<PopularSearchAdapter.PopularSearchViewHolder>() {

    private var items: List<SearchQuery> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.popular_search_item, parent, false)
        return PopularSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopularSearchViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position + 1) // position + 1 to show rank
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<SearchQuery>) {
        val diffCallback = PopularSearchDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    inner class PopularSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.tv_rank)
        private val queryText: TextView = itemView.findViewById(R.id.tv_query)

        fun bind(item: SearchQuery, rank: Int) {
            rankText.text = "${rank}위"
            queryText.text = item.query
            itemView.setOnClickListener {
                // 클릭 이벤트 처리
            }
        }
    }

    class PopularSearchDiffCallback(
        private val oldList: List<SearchQuery>,
        private val newList: List<SearchQuery>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].query == newList[newItemPosition].query
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}