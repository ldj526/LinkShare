package com.example.linkshare.comment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.util.FBAuth
import com.example.linkshare.util.FBUser

class CommentRVAdapter(var commentList: MutableList<Comment>) :
    RecyclerView.Adapter<CommentRVAdapter.ViewHolder>() {

    fun setCommentData(newCommentList: MutableList<Comment>) {
        val diffCallback = CommentDiffCallback(commentList, newCommentList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        commentList.clear()
        commentList.addAll(newCommentList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = itemView.findViewById<TextView>(R.id.tv_comment_title)
        val nickname = itemView.findViewById<TextView>(R.id.tv_user_nickname)
        val time = itemView.findViewById<TextView>(R.id.tv_comment_time)
        val delete = itemView.findViewById<ImageView>(R.id.iv_comment_delete)

        fun bind(comment: Comment) {
            title.text = comment.comment
            time.text = comment.time

            FBUser.getUserNickname(comment.uid, onSuccess = { userNickname ->
                nickname.text = userNickname ?: "알 수 없음"
            }, onFailure = { exception ->
                Log.e("CommentRVADatper", "닉네임 가져오기 실패", exception)
                nickname.text = "알 수 없음"
            })
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

    class CommentDiffCallback(
        private val oldList: List<Comment>,
        private val newList: List<Comment>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].uid == newList[newItemPosition].uid
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}