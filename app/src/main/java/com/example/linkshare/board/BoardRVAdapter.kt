package com.example.linkshare.board

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.linkshare.R
import com.example.linkshare.link.Link

class BoardRVAdapter(var linkList: MutableList<Link>) :
    RecyclerView.Adapter<BoardRVAdapter.ViewHolder>() {

    fun setBoardData(newLinkList: MutableList<Link>) {
        val diffCallback = BoardDiffCallback(linkList, newLinkList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        linkList.clear()
        linkList.addAll(newLinkList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = itemView.findViewById<TextView>(R.id.tv_board_title)
        val link = itemView.findViewById<TextView>(R.id.tv_board_link)
        val time = itemView.findViewById<TextView>(R.id.tv_board_time)

        fun bind(link: Link) {
            title.text = link.title
            this.link.text = link.link
            time.text = link.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.board_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memo = linkList[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, BoardActivity::class.java)
            intent.putExtra("key", memo.key)   // key 값 전달
            it.context.startActivity(intent)
        }
        holder.bind(linkList[position])
    }

    override fun getItemCount(): Int = linkList.size

    class BoardDiffCallback(
        private val oldList: List<Link>,
        private val newList: List<Link>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // 메모의 고유 ID 또는 기타 고유한 속성을 비교
            return oldList[oldItemPosition].key == newList[newItemPosition].key
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // 메모의 내용이 같은지 비교
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}