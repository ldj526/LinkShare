package com.example.linkshare.board

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.memo.Memo
import com.example.linkshare.memo.MemoActivity

class BoardRVAdapter(var memoList: MutableList<Memo>) :
    RecyclerView.Adapter<BoardRVAdapter.ViewHolder>() {

    fun setBoardData(data: MutableList<Memo>) {
        memoList = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = itemView.findViewById<TextView>(R.id.tv_board_title)
        val link = itemView.findViewById<TextView>(R.id.tv_board_link)
        val time = itemView.findViewById<TextView>(R.id.tv_board_time)

        fun bind(memo: Memo) {
            title.text = memo.title
            link.text = memo.link
            time.text = memo.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.board_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memo = memoList[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, MemoActivity::class.java)
            intent.putExtra("key", memo.key)   // key 값 전달
            it.context.startActivity(intent)
        }
        holder.bind(memoList[position])
    }

    override fun getItemCount(): Int = memoList.size
}