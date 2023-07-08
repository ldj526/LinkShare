package com.example.linkshare.board.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.utils.FBAuth

class CommentRVAdapter(var commentList: MutableList<CommentModel>) :
    RecyclerView.Adapter<CommentRVAdapter.ViewHolder>() {

    fun setData(data: MutableList<CommentModel>) {
        commentList = data
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleArea)
        val time: TextView = view.findViewById(R.id.timeArea)
        val delete: ImageView = view.findViewById(R.id.commentDelete)

        fun bind(item: CommentModel) {
            title.text = item.commentTitle
            time.text = item.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentRVAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.comment_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentRVAdapter.ViewHolder, position: Int) {

        holder.delete.setOnClickListener {
            itemClickListener.onClick(it, position)
        }

        // 내가 쓴 댓글일 경우 삭제 버튼 표시
        if (commentList[position].uid == FBAuth.getUid()) {
            holder.delete.visibility = View.VISIBLE
        }

        holder.bind(commentList[position])
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

    override fun getItemCount(): Int = commentList.size
}