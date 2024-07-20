package com.example.linkshare.main

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
import com.example.linkshare.board.BoardActivity
import com.example.linkshare.board.BoardViewModel
import com.example.linkshare.link.Link

class TopLinksAdapter(
    private var linkList: MutableList<Link>,
    private val boardViewModel: BoardViewModel
) : RecyclerView.Adapter<TopLinksAdapter.ViewHolder>() {

    fun updateLinks(newLinks: MutableList<Link>) {
        val diffCallback = LinkDiffCallback(linkList, newLinks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        linkList.clear()
        linkList.addAll(newLinks)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.top_image_view)
        val title: TextView = view.findViewById(R.id.top_tv_title)
        val progressBar: ProgressBar = view.findViewById(R.id.top_progress_bar)

        fun bind(link: Link) {
            title.text = link.title

            Glide.with(itemView.context)
                .load(link.imageUrl)
                .placeholder(R.drawable.loading_image)  // 로딩 중
                .error(R.drawable.error_image)        // 로딩 실패
                .fallback(R.drawable.no_image)     // 이미지가 null 일 경우
                .listener(object : RequestListener<Drawable> {
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val link = linkList[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, BoardActivity::class.java)
            intent.putExtra("linkId", link.key)   // key 값 전달
            intent.putExtra("writeUid", link.uid)   // 글 uid 값 전달
            boardViewModel.increaseViewCount(link.uid, link.key)
            it.context.startActivity(intent)
        }
        holder.bind(linkList[position])
    }

    override fun getItemCount(): Int = linkList.size

    class LinkDiffCallback(
        private val oldList: List<Link>,
        private val newList: List<Link>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].key == newList[newItemPosition].key
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}