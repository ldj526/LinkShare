package com.example.linkshare.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R

class BoardRVAdapter(val boardList: MutableList<Board>) :
    RecyclerView.Adapter<BoardRVAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val link = itemView.findViewById<TextView>(R.id.tv_link)
        val time = itemView.findViewById<TextView>(R.id.tv_time)

        fun bind(board: Board) {
            title.text = board.title
            link.text = board.link
            time.text = board.time
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
        holder.bind(boardList[position])
    }

    // 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    // 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener: OnItemClickListener

    override fun getItemCount(): Int = boardList.size
}