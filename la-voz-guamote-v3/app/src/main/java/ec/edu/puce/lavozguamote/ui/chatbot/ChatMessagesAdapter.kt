package ec.edu.puce.lavozguamote.ui.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ec.edu.puce.lavozguamote.databinding.ItemChatMessageBinding

class ChatMessagesAdapter : ListAdapter<ChatMessage, ChatMessagesAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            if (message.isBot) {
                // Mensaje del bot (izquierda)
                binding.layoutUserMessage.visibility = View.GONE
                binding.layoutBotMessage.visibility = View.VISIBLE
                binding.tvBotMessage.text = message.text
                binding.tvBotTime.text = message.getFormattedTime()
            } else {
                // Mensaje del usuario (derecha)
                binding.layoutUserMessage.visibility = View.VISIBLE
                binding.layoutBotMessage.visibility = View.GONE
                binding.tvUserMessage.text = message.text
                binding.tvUserTime.text = message.getFormattedTime()
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id && oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
