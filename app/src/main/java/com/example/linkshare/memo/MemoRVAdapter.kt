package com.example.linkshare.memo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R

class MemoRVAdapter(val memoList: MutableList<MemoModel>) :
    RecyclerView.Adapter<MemoRVAdapter.ViewHolder>() {
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.title)
        val content = view.findViewById<TextView>(R.id.content)
        val time = view.findViewById<TextView>(R.id.time)

        fun bind(item: MemoModel) {
            title.text = item.title
            content.text = item.content
            time.text = item.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoRVAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.memo_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoRVAdapter.ViewHolder, position: Int) {
        holder.bind(memoList[position])
    }

    override fun getItemCount(): Int = memoList.size
}