package com.example.linkshare.memo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.linkshare.R

class MemoLVAdapter(val memoList: MutableList<MemoModel>) : BaseAdapter() {
    override fun getCount() = memoList.size

    override fun getItem(position: Int): Any {
        return memoList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view =
                LayoutInflater.from(parent?.context).inflate(R.layout.memo_list_item, parent, false)
        }

        val title = view?.findViewById<TextView>(R.id.title)
        val content = view?.findViewById<TextView>(R.id.content)
        val time = view?.findViewById<TextView>(R.id.time)

        title!!.text = memoList[position].title
        content!!.text = memoList[position].content
        time!!.text = memoList[position].time

        return view!!
    }

}