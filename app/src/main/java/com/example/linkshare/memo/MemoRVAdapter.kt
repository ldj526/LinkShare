package com.example.linkshare.memo

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R

class MemoRVAdapter(var memoList: MutableList<Memo>) :
    RecyclerView.Adapter<MemoRVAdapter.ViewHolder>() {

    fun setMemoData(data: MutableList<Memo>) {
        memoList = data
        Log.d("memoData", "setData: $memoList")
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title = itemView.findViewById<TextView>(R.id.tv_memo_title)
        val link = itemView.findViewById<TextView>(R.id.tv_memo_link)
        val time = itemView.findViewById<TextView>(R.id.tv_memo_time)

        fun bind(memo: Memo) {
            title.text = memo.title
            link.text = memo.link
            time.text = memo.time
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memo = memoList[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, MemoActivity::class.java)
            intent.putExtra("key", memo.key)   // key 값 전달
            intent.putExtra("category", memo.category)  // category 값 전달
            it.context.startActivity(intent)
        }
        holder.bind(memoList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.memo_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = memoList.size
}