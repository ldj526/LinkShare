package com.example.linkshare.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.util.FBAuth

class CommentRVAdapter(var commentList: MutableList<Comment>) :
    RecyclerView.Adapter<CommentRVAdapter.ViewHolder>() {

    fun setCommentData(data: MutableList<Comment>) {
        commentList = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = itemView.findViewById<TextView>(R.id.tv_comment_title)
        val time = itemView.findViewById<TextView>(R.id.tv_comment_time)
        val delete = itemView.findViewById<ImageView>(R.id.iv_comment_delete)

        fun bind(comment: Comment) {
            title.text = comment.comment
            time.text = comment.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.comment_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = commentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (commentList[position].uid == FBAuth.getUid()) {
            holder.delete.visibility = View.VISIBLE
        }
        holder.bind(commentList[position])
    }
}