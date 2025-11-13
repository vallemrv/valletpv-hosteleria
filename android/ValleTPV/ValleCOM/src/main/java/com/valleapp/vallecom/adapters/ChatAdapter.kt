package com.valleapp.vallecom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.valleapp.valletpv.R

data class ChatMessage(
    val message: String,
    val isFromUser: Boolean,
    val messageId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutUserMessage: LinearLayout = view.findViewById(R.id.layoutUserMessage)
        val layoutBotMessage: LinearLayout = view.findViewById(R.id.layoutBotMessage)
        val textViewUserMessage: TextView = view.findViewById(R.id.textViewUserMessage)
        val textViewBotMessage: TextView = view.findViewById(R.id.textViewBotMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]

        if (message.isFromUser) {
            // Mostrar mensaje del usuario
            holder.layoutUserMessage.visibility = View.VISIBLE
            holder.layoutBotMessage.visibility = View.GONE
            holder.textViewUserMessage.text = message.message
        } else {
            // Mostrar mensaje del bot
            holder.layoutUserMessage.visibility = View.GONE
            holder.layoutBotMessage.visibility = View.VISIBLE
            holder.textViewBotMessage.text = message.message
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}

