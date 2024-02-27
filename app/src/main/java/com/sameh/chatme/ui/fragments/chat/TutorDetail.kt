package com.sameh.chatme.ui.fragments.chat

import OnVideoUploadListener
import PushNotificationSender
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sameh.chatme.data.model.User
import com.sameh.chatme.databinding.TutorDetailBinding
import com.sameh.chatme.ui.alertdialog.LoadingAlertDialog
import com.sameh.chatme.ui.fragments.chat.adapter.MessageAdapter
import com.sameh.chatme.ui.presenter.FirebaseRealTimeChatPresenter
import com.sameh.chatme.ui.viewModel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL


@AndroidEntryPoint
class TutorDetail : Fragment(), FirebaseRealTimeChatPresenter {

    private lateinit var binding: TutorDetailBinding

    private val args by navArgs<TutorDetailArgs>()

    private val chatViewModel: ChatViewModel by viewModels()

    private lateinit var messageAdapter: MessageAdapter

    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String

    private val currentUserId = Firebase.auth.currentUser!!.uid

    private lateinit var currentUser: User

    private  val pushNotificationSender = PushNotificationSender()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TutorDetailBinding.inflate(inflater, container, false)

        chatViewModel.repo.firebaseRealTimeDatabase.firebaseRealTimeChatPresenter = this
        chatViewModel.getCurrentUser()

        senderRoom = currentUserId + args.user.id
        receiverRoom = args.user.id + currentUserId
        chatViewModel.getMessages(senderRoom)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatViewModel.currentUserLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUser = it
            }
        }
        if (args.user.videoUrl == null) {
            binding.textView5.setVisibility(View.GONE)
            binding.webView.setVisibility(View.GONE)
        }

        setDataInUi()
        setUpRecyclerView()

        chatViewModel.messagesLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                messageAdapter.setMessagesListToAdapter(it)
//                binding.recChat.scrollToPosition(messageAdapter.itemCount - 1)
            }
        }

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            goToLatestMessageFragment()
//        }
//
        binding.imgBtnBack.setOnClickListener {
            goToTutorList()
        }
        binding.btnChat.setOnClickListener {
            if (args.user.email == currentUser.email){
                Toast.makeText(requireContext(), "Unable to chat to yourself", Toast.LENGTH_SHORT).show()
            }else{
                goToTutorChat(args.user)
            }
        }

        binding.callV.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Meeting room")
            builder.setMessage("Are you sure to Join ${args.user.username} Meeting room ?")
            builder.setPositiveButton("Yes") { _, _ ->
                startVideoCall()
            }
            builder.setNegativeButton("No") { _, _ -> }
            builder.show()
        }

//

    }

    private fun setDataInUi() {
        binding.tvUsername.text = args.user.username
        binding.tvSubject.text= "Subject: "+args.user.subject
        binding.tvClass.text="Level: "+args.user.teachClass
        binding.tvPhone.text="Tel: "+args.user.phonenumber
        binding.tvLineid.text="Line ID: "+ args.user.lineid
        binding.tvBd.text="Birthday: "+args.user.birthday
        if (args.user.description != null && args.user.description != "") {
            binding.edDescription.setText(args.user.description)
            binding.edDescription.keyListener = null
        }else{
            binding.edDescription.visibility = View.GONE
            binding.textInputLayoutDescription.visibility = View.GONE
        }
        if (args.user.profileImgUrl != null) {
            binding.cirImgUser.load(args.user.profileImgUrl)
        }
        if (args.user.videoUrl != null) {
//            setVideoView(args.user?.videoUrl)
            binding.webView.setOnLongClickListener {
                try {
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=4as2bcQOj00"))
                    startActivity(myIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "Can't", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
                true

            }

            binding.webView.webViewClient = WebViewClient()

            // this will load the url of the website
        //    binding.webView.loadUrl("https://www.youtube.com/watch?v=4as2bcQOj00")
            args.user.videoUrl?.let { useYouTubePlayerAPI(it) }

            // this will enable the javascript settings, it can also allow xss vulnerabilities
            binding.webView.settings.javaScriptEnabled = true

            // if you want to enable zoom feature
            binding.webView.settings.setSupportZoom(true)



            if (args.user.videoUrl == null) {
                binding.textView5.setVisibility(View.GONE)
                binding.webView.setVisibility(View.GONE)
            }


            // if you press Back button this code will work

        }

    }

//    private fun setVideoView(uri : String?) {
//        val mediaController = MediaController(requireContext())
//        mediaController.setAnchorView(binding.videoView2)
//        binding.videoView2.setMediaController(mediaController)
//        binding.videoView2.setVideoPath(uri)
//        binding.videoView2.requestFocus()
//        binding.videoView2.setOnPreparedListener(MediaPlayer.OnPreparedListener {
//            binding.videoView2.pause()
//        })
//    }

    private fun setUpRecyclerView() {

        messageAdapter = MessageAdapter(requireContext(), args.user.id)
//        binding.recChat.adapter = messageAdapter
//        binding.recChat.layoutManager =
//            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
//
//        binding.recChat.itemAnimator = SlideInUpAnimator().apply {
//            addDuration = 200
//        }
    }

    private fun goToTutorList() {
        val action = TutorDetailDirections.actionTutorDetailToTutorList()
        findNavController().navigate(action)
    }

    private fun goToTutorChat(user: User) {
        val action = TutorDetailDirections.actionTutorDetailToChat(user)
        findNavController().navigate(action)
    }

    override fun ifSaveMessageToDatabaseSuccess(ifSuccess: Boolean, state: String) {
//        if (ifSuccess) {
//            binding.recChat.scrollToPosition(messageAdapter.itemCount - 1)
//        } else {
//            Toast.makeText(requireContext(), state, Toast.LENGTH_SHORT).show()
//        }
    }


    class JavaScriptInterface(private val listener: OnVideoUploadListener) {
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        fun onVideoUploaded(id: String?, url: String?) {
            // ทำอะไรก็ตามที่คุณต้องการเมื่อได้รับ VideoId หรือ videoUrl จากการอัปโหลด
        }
    }

    // ใช้งาน WebView และ JavaScript Interface
    private fun useYouTubePlayerAPI(state: String) {
        val webView = WebView(requireContext())
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(state)
        binding.webView.addView(webView)
    }

    private fun startVideoCall() {
        if (args.user.email != currentUser.email){
            chatViewModel.sendMessage("${currentUser.username} is join Meeting\nTap here to join too.", senderRoom, receiverRoom, args.user, currentUser)
            pushNotificationSender.sendNotificationToUser(args.user.id, "${currentUser.username} is join your Meeting." , "Meeting")
        }
        val roomName = args.user.id

        val userInfo = JitsiMeetUserInfo()
        userInfo.displayName = "${currentUser.username}"
        userInfo.email = "${currentUser.email}"
        currentUser.profileImgUrl?.let {userInfo.avatar = URL(currentUser.profileImgUrl)}


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
        JitsiMeetActivity.launch(requireContext(), options)
    }




}