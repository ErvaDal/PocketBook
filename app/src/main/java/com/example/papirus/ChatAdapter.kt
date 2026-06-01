package com.example.papirus

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

// Mesaj Modeli
data class ChatMessage(val text: String, val isUser: Boolean)

// Mesaj Adaptörü
class ChatAdapter(private val messages: MutableList<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llContainer: LinearLayout = view.findViewById(R.id.ll_message_container)
        val cvBubble: MaterialCardView = view.findViewById(R.id.cv_message_bubble)
        val tvMessage: TextView = view.findViewById(R.id.tv_chat_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.text

        val layoutParams = holder.cvBubble.layoutParams as LinearLayout.LayoutParams

        if (message.isUser) {
            // Kullanıcı mesajları sağa yaslanır ve rengi farklı olur
            layoutParams.gravity = Gravity.END
            holder.cvBubble.setCardBackgroundColor(Color.parseColor("#E3F2FD")) // Açık Mavi
            holder.tvMessage.setTextColor(Color.BLACK)
        } else {
            // AI mesajları sola yaslanır
            layoutParams.gravity = Gravity.START
            holder.cvBubble.setCardBackgroundColor(Color.parseColor("#F5F5F5")) // Açık Gri
            holder.tvMessage.setTextColor(Color.BLACK)
        }
        holder.cvBubble.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }
}