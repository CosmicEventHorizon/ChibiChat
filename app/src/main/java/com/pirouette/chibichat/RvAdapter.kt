package com.pirouette.chibichat


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RvAdapter (val msgArray: List<Message>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RvAdapter.ViewHolder, position: Int) {
        val msgContentHolder = msgArray[position]

        // sets the text to the textview from our itemHolder class
        holder.textView.text = msgContentHolder.msgContent
        if (msgContentHolder.user == 0) {
            holder.textView.setTextColor(Color.parseColor("#FFBB86FC"))
        } else if (msgContentHolder.user == 1) {
            holder.textView.setTextColor(Color.parseColor("#008000"))
        } else if (msgContentHolder.user == 2) {
            holder.textView.setTextColor(Color.parseColor("#FFFFFFFF"))
        }
    }

    override fun getItemCount(): Int {
        return msgArray.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val textView: TextView = itemView.findViewById(R.id.tvMessages)


    }
}