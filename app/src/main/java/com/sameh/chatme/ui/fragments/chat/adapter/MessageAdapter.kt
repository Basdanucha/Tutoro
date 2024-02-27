package com.sameh.chatme.ui.fragments.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sameh.chatme.R
import com.sameh.chatme.constants.Constants
import com.sameh.chatme.data.model.Message
import com.sameh.chatme.data.model.User
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL
import java.time.format.DateTimeFormatter

class MessageAdapter(
    val context: Context,
    private val senderId: String,
    val currentUser : User? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messagesList = ArrayList<Message?>()

    private var messagesDiffUtil: MessagesDiffUtil? = null

    fun setMessagesListToAdapter(newMessagesList: ArrayList<Message?>) {
        messagesDiffUtil = MessagesDiffUtil(
            messagesList,
            newMessagesList
        )
        val diffResult = DiffUtil.calculateDiff(messagesDiffUtil!!)
        this.messagesList = newMessagesList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Constants.SEND_MESSAGE) {
            val view: View = LayoutInflater.from(context).inflate(
                R.layout.send_message,
                parent,
                false
            )
            SendViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(
                R.layout.recieve_message,
                parent,
                false
            )
            ReceiveViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messagesList[position]

        if (holder.javaClass == SendViewHolder::class.java) {

            val viewHolder = holder as SendViewHolder
            holder.sendMessage.text = currentMessage?.message
            holder.sendMessage.setOnClickListener {
                holder.sendMessageTime.isVisible = !holder.sendMessageTime.isVisible

            }
            var index = 0
            for (amessage in messagesList){
                if (index == position){
                    holder.sendMessageTime.text = currentMessage?.messageTimeDate
                    continue
                }
                index++
                if (currentMessage?.messageTimeDate?.substring(0,12) == amessage?.messageTimeDate?.substring(0,12) ){
                    holder.sendMessageTime.isVisible = false
                }else if(currentMessage?.messageTimeDate?.substring(0,12) != amessage?.messageTimeDate?.substring(0,12)){
                    holder.sendMessageTime.text = currentMessage?.messageTimeDate
                    holder.sendMessageTime.isVisible = true
                }

            }
            holder.sendMessageTime.text = currentMessage?.messageTimeDate
        } else {
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = currentMessage?.message
            if (currentMessage?.message?.contains("join Meeting") == true && currentUser !=null){
                holder.receiveMessage.setTextColor(ContextCompat.getColor(context, R.color.button_background))
                holder.receiveMessage.setOnClickListener {
                    startVideoCall()
                }
            }else{
                holder.receiveMessage.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.receiveMessage.setOnClickListener {
                    holder.receiveMessageTime.isVisible = !holder.receiveMessageTime.isVisible
                }
            }
            var index = 0
            for (amessage in messagesList){
                if (index == position){
                    holder.receiveMessageTime.text = currentMessage?.messageTimeDate
                    continue
                }
                index++
                if (currentMessage?.messageTimeDate?.substring(0,12) == amessage?.messageTimeDate?.substring(0,12) ){
                    holder.receiveMessageTime.isVisible = false
                }else if(currentMessage?.messageTimeDate?.substring(0,12) != amessage?.messageTimeDate?.substring(0,12)){
                    holder.receiveMessageTime.text = currentMessage?.messageTimeDate
                    holder.receiveMessageTime.isVisible = true
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentMessage = messagesList[position]
        return if (senderId == currentMessage!!.senderId) {
            Constants.RECEIVE_MESSAGE
        } else {
            Constants.SEND_MESSAGE
        }

        //return super.getItemViewType(position)
    }


    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage: TextView = itemView.findViewById(R.id.tv_send_message)
        val sendMessageTime: TextView = itemView.findViewById(R.id.time)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.tv_receive_message)
        val receiveMessageTime: TextView = itemView.findViewById(R.id.rTime)
    }

    private fun startVideoCall() {
        val roomName = "${currentUser?.id}"

        val userInfo = JitsiMeetUserInfo()
        userInfo.displayName = "${currentUser?.username}"
        userInfo.email = "${currentUser?.email}"
        currentUser?.profileImgUrl?.let {userInfo.avatar = URL(it)}

        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(roomName)
            .setServerURL(URL("https://meet.mayfirst.org"))
            .setUserInfo(userInfo)
            .setFeatureFlag("live-streaming", false) // Disable live streaming
            .setFeatureFlag("recording", false) // Disable recording
            .setFeatureFlag("prejoinpage.enabled", false)
            .setFeatureFlag("security", true)
            .setFeatureFlag("meeting-password.enabled", false)
            .setFeatureFlag("lobby-mode.enabled", false)
            .setAudioMuted(true) // Start with audio muted
            .setVideoMuted(true) // Start with video muted
            .build()
        JitsiMeetActivity.launch(context, options)
    }

}