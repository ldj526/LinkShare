package com.example.linkshare.memo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R

class MemoListAdapter(val memoList: MutableList<Memo>): RecyclerView.Adapter<MemoListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val link = itemView.findViewById<TextView>(R.id.tv_link)
        val time = itemView.findViewById<TextView>(R.id.tv_time)

        fun bind(memo: Memo) {
            title.text = memo.title
            link.text = memo.link
            time.text = memo.time
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(memoList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.memo_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = memoList.size


}