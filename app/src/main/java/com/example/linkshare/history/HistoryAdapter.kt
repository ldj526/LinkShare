package com.example.linkshare.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R

class HistoryAdapter(
    val historyItems: MutableList<HistoryItem>,
    private val onDeleteClick: (HistoryItem) -> Unit
) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    fun updateHistoryItems(newHistoryItems: List<HistoryItem>) {
        val diffCallback = HistoryDiffCallback(historyItems, newHistoryItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        historyItems.clear()
        historyItems.addAll(newHistoryItems)

        diffResult.dispatchUpdatesTo(this)
    }

    fun removeItem(position: Int) {
        if (position < 0 || position >= historyItems.size) return

        historyItems.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = itemView.findViewById(R.id.tv_history_title)
        val url: TextView = itemView.findViewById(R.id.tv_history_url)
        val delete: Button = itemView.findViewById(R.id.btn_delete_history)

        fun bind(historyItem: HistoryItem, onDeleteClick: (HistoryItem) -> Unit) {
            title.text = historyItem.title
            url.text = historyItem.url

            delete.setOnClickListener {
                onDeleteClick(historyItem)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = historyItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyItem = historyItems[position]
        holder.bind(historyItem, onDeleteClick)
    }

    class HistoryDiffCallback(
        private val oldList: List<HistoryItem>,
        private val newList: List<HistoryItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}