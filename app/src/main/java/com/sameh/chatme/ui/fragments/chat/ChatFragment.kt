package com.sameh.chatme.ui.fragments.chat

import PushNotificationSender
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sameh.chatme.data.model.User
import com.sameh.chatme.databinding.FragmentChatBinding
import com.sameh.chatme.ui.alertdialog.LoadingAlertDialog
import com.sameh.chatme.ui.fragments.TutorList.TutorListFragmentDirections
import com.sameh.chatme.ui.fragments.chat.adapter.MessageAdapter
import com.sameh.chatme.ui.presenter.FirebaseRealTimeChatPresenter
import com.sameh.chatme.ui.viewModel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL


@AndroidEntryPoint
class ChatFragment : Fragment(), FirebaseRealTimeChatPresenter {

    private lateinit var binding: FragmentChatBinding

    private val args by navArgs<ChatFragmentArgs>()

    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var messageAdapter: MessageAdapter

    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String

    private val currentUserId = Firebase.auth.currentUser!!.uid

    private lateinit var currentUser: User

    private lateinit var loadingAlertDialog: LoadingAlertDialog

    private  val pushNotificationSender = PushNotificationSender()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        chatViewModel.repo.firebaseRealTimeDatabase.firebaseRealTimeChatPresenter = this
        chatViewModel.getCurrentUser()

        senderRoom = currentUserId + args.user.id
        receiverRoom = args.user.id + currentUserId
        chatViewModel.getMessages(senderRoom)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.currentUserLiveData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentUser = user
                setUpRecyclerView(currentUser)
                setDataInUi()
                chatViewModel.messagesLiveData.observe(viewLifecycleOwner) {
                    if (it != null) {
                        messageAdapter.setMessagesListToAdapter(it)
                        binding.recChat.scrollToPosition(messageAdapter.itemCount - 1)
                    }
            }

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goToLatestMessageFragment()
        }

        binding.imgBtnBack.setOnClickListener {
            goToLatestMessageFragment()
        }

        binding.cirImgUser.setOnClickListener {
            if (args.user.subject !=null) {
                goToChatTutor(user = args.user)
            }else{
                Toast.makeText(requireContext(), "${args.user.username} is not a tutor.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSend.setOnClickListener {
            val message = binding.edMessage.text.toString()
            if (message.isNotBlank() && message.isNotEmpty()) {
                chatViewModel.sendMessage(message, senderRoom, receiverRoom, args.user, currentUser)
                binding.edMessage.setText("")
                pushNotificationSender.sendNotificationToUser(args.user.id, message , "${currentUser.username}")
            } else {
                binding.edMessage.error = "Enter message"
            }
        }

        binding.btnVideoCall.setOnClickListener {
            startVideoCall()
        }
    }

    private fun setDataInUi() {
        binding.tvUsername.text = args.user.username
        if (args.user.profileImgUrl != null) {
            binding.cirImgUser.load(args.user.profileImgUrl)
        }

    }

    private fun setUpRecyclerView(you : User? =null) {

        messageAdapter = MessageAdapter(requireContext(), args.user.id , you)
        binding.recChat.adapter = messageAdapter
        binding.recChat.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        binding.recChat.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 200
        }
    }

    private fun goToLatestMessageFragment() {
        val action = ChatFragmentDirections.actionChatFragmentToLatestMessagesFragment()
        findNavController().navigate(action)
    }

    private fun goToChatTutor(user: User) {
        val action = ChatFragmentDirections.actionChatFragmentToTutor(user)
        findNavController().navigate(action)
    }

    override fun ifSaveMessageToDatabaseSuccess(ifSuccess: Boolean, state: String) {
        if (ifSuccess) {
            binding.recChat.scrollToPosition(messageAdapter.itemCount - 1)
        } else {
            Toast.makeText(requireContext(), state, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVideoCall() {
        chatViewModel.sendMessage("${currentUser.username} is join Meeting\nTap here to join too.", senderRoom, receiverRoom, args.user, currentUser)
        pushNotificationSender.sendNotificationToUser(args.user.id, "${currentUser.username} is join your Meeting." , "Meeting")
        val roomName = "${args.user.id}"

        val userInfo = JitsiMeetUserInfo()
        userInfo.displayName = "${currentUser.username}"
        userInfo.email = "${currentUser.email}"
        currentUser.profileImgUrl?.let { userInfo.avatar = URL(it)}

        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(roomName)
            .setServerURL(URL("https://meet.mayfirst.org"))
            .setUserInfo(userInfo)
            .setFeatureFlag("live-streaming", false) // Disable live streaming
            .setFeatureFlag("recording", false) // Disable recording
            .setFeatureFlag("security", false) // Disable security (no password required)
            .setFeatureFlag("meeting-password.enabled", false)
            .setAudioMuted(true) // Start with audio muted
            .setVideoMuted(true) // Start with video muted
            .build()
        JitsiMeetActivity.launch(requireContext(), options)
    }




}