package com.sameh.chatme.ui.fragments.latestmessages.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sameh.chatme.data.model.LatestUserMessage
import com.sameh.chatme.databinding.LatestUsersLayoutBinding
import javax.inject.Inject

class LatestUserAndMessagesAdapter @Inject constructor(
    private val context: Application
) : ListAdapter<LatestUserMessage, LatestUserAndMessagesAdapter.ViewHolder>(
    LatestUserAndMessagesDiffUtil()
) {

    private val currentUser = Firebase.auth.currentUser

    var onUserClickListener: OnLatestUserClickListener? = null

    fun setData(latestUsersList: ArrayList<LatestUserMessage?>) {
        this.submitList(latestUsersList.sortedByDescending { it?.message?.messageTimeDate })
    }

    inner class ViewHolder(private val binding: LatestUsersLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(latestUserMessage: LatestUserMessage) {
            if (latestUserMessage.user.profileImgUrl != null) {
                binding.cirImageUser.load(latestUserMessage.user.profileImgUrl)
            }
            if (latestUserMessage.message.senderId == currentUser?.uid) {
                val message = "You: " + latestUserMessage.message.message.replace("\n","")
                binding.tvLatestMessage.text = message
            } else {
                binding.tvLatestMessage.text = latestUserMessage.message.message.replace("\n","")
            }
            binding.tvUserName.text = latestUserMessage.user.username

            binding.tvMessageDate.text = latestUserMessage.message.messageTimeDate

            binding.root.setOnClickListener {
                onUserClickListener?.onLatestUserClickListener(latestUserMessage)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LatestUsersLayoutBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

}

class LatestUserAndMessagesDiffUtil : DiffUtil.ItemCallback<LatestUserMessage>() {

    override fun areItemsTheSame(oldItem: LatestUserMessage, newItem: LatestUserMessage): Boolean {
        return oldItem.message.messageTimeDate == newItem.message.messageTimeDate
    }

    override fun areContentsTheSame(
        oldItem: LatestUserMessage,
        newItem: LatestUserMessage
    ): Boolean {
        return oldItem == newItem
    }

}