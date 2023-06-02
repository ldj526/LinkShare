package com.example.linkshare.memo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
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
        return view!!
    }

}