package com.example.linkshare.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.memo.Memo

class BoardRVAdapter(val memoList: MutableList<Memo>) :
    RecyclerView.Adapter<BoardRVAdapter.ViewHolder>() {

    private lateinit var itemClickListener: OnItemClickListener

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
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
        holder.bind(memoList[position])
    }

    // 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    // 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    override fun getItemCount(): Int = memoList.size
}