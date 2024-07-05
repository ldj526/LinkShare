package com.example.linkshare.board

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.linkshare.R
import com.example.linkshare.link.Link
import com.example.linkshare.util.FBAuth

class BoardRVAdapter(
    var linkList: MutableList<Link>,
    private val boardViewModel: BoardViewModel
) : RecyclerView.Adapter<BoardRVAdapter.ViewHolder>() {

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
        val image = itemView.findViewById<ImageView>(R.id.iv_board_image)
        val progressBar = itemView.findViewById<ProgressBar>(R.id.image_board_progressBar)
        val shareCount = itemView.findViewById<TextView>(R.id.tv_share_count)

        fun bind(link: Link) {
            title.text = link.title
            this.link.text = link.link
            time.text = FBAuth.formatTimestamp(link.time)
            shareCount.text = "공유 : ${link.shareCount}"

            progressBar.visibility = View.VISIBLE

            Glide.with(itemView.context)
                .load(link.imageUrl)
                .placeholder(R.drawable.loading_image)  // 로딩 중
                .error(R.drawable.error_image)        // 로딩 실패
                .fallback(R.drawable.no_image)     // 이미지가 null 일 경우
                .listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(image)
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
            intent.putExtra("linkId", memo.key)   // key 값 전달
            intent.putExtra("writeUid", memo.uid)   // 글 uid 값 전달
            boardViewModel.increaseViewCount(memo.uid, memo.key)
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