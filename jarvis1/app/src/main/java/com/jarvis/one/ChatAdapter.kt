package com.jarvis.one

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val items: List<Msg>) : RecyclerView.Adapter<ChatAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tv: TextView = v.findViewById(R.id.tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val m = items[position]
        h.tv.text = m.text
        val lp = h.tv.layoutParams as FrameLayout.LayoutParams
        lp.gravity = if (m.user) Gravity.END else Gravity.START
        h.tv.layoutParams = lp
        h.tv.setBackgroundResource(if (m.user) R.drawable.bubble_user else R.drawable.bubble_ai)
    }
}
