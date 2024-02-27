package com.sameh.chatme.ui.fragments.latestmessages

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sameh.chatme.R
import com.sameh.chatme.data.model.LatestUserMessage
import com.sameh.chatme.data.model.User
import com.sameh.chatme.databinding.FragmentLatestMessagesBinding
import com.sameh.chatme.ui.alertdialog.LoadingAlertDialog
import com.sameh.chatme.ui.fragments.latestmessages.adapter.LatestUserAndMessagesAdapter
import com.sameh.chatme.ui.fragments.latestmessages.adapter.OnLatestUserClickListener
import com.sameh.chatme.ui.viewModel.LatestMessageViewModel
import com.sameh.chatme.ui.viewModel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class LatestMessagesFragment : Fragment(), OnLatestUserClickListener {

    private lateinit var binding: FragmentLatestMessagesBinding

    private val latestMessageViewModel: LatestMessageViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var latestUserAndMessagesAdapter: LatestUserAndMessagesAdapter

    private lateinit var loadingAlertDialog: LoadingAlertDialog

    @Inject
    lateinit var auth: FirebaseAuth

    private val currentUser: FirebaseUser? = Firebase.auth.currentUser

    private var currentUsers: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLatestMessagesBinding.inflate(inflater, container, false)

        latestMessageViewModel.getCurrentUser()
        latestMessageViewModel.getLatestUserAndMessages(currentUser!!.uid)
        profileViewModel.getCurrentUser()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        profileViewModel.currentUserLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUsers = it
            }}

        binding.cirImgUser.setOnClickListener {
            if (currentUsers?.subject != null){
                goToProfileTutorFragment()
            }else{
                goToProfileFragment()
            }
        }


        latestMessageViewModel.currentUserProfileImageLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.cirImgUser.load(it)
            }
        }

        latestMessageViewModel.latestUserAndMessagesLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                latestUserAndMessagesAdapter.setData(it)
            }
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.new_message_menu -> {
                    goToNewMessageFragment()
                }
                R.id.logout_menu -> {
                    auth.signOut()
                    Toast.makeText(requireContext(), "Sign out", Toast.LENGTH_SHORT).show()
                    goToLoginFragment()
                }
                R.id.new_tutor-> {
                    goToTutorListFragment()
                }
                R.id.yourMeetingRoom ->{
                    startVideoCall()
                }
            }
            true
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    latestMessageViewModel.searchOnLatestUserAndMessages(currentUser!!.uid, query)

                    latestMessageViewModel.searchOnUsersLiveData.observe(viewLifecycleOwner) {
                        if (it != null) {
                            latestUserAndMessagesAdapter.setData(it)
                        }
                    }
                } else {
                    latestMessageViewModel.getLatestUserAndMessages(currentUser!!.uid)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    latestMessageViewModel.searchOnLatestUserAndMessages(currentUser!!.uid, newText)
                    latestMessageViewModel.searchOnUsersLiveData.observe(viewLifecycleOwner) {
                        if (it != null) {
                            latestUserAndMessagesAdapter.setData(it)
                        }
                    }
                } else {
                    latestMessageViewModel.getLatestUserAndMessages(currentUser!!.uid)
                }
                return true
            }
        })

    }

    private fun setupRecyclerView() {
        binding.latestUsersRecView.adapter = latestUserAndMessagesAdapter
        latestUserAndMessagesAdapter.onUserClickListener = this
        binding.latestUsersRecView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        binding.latestUsersRecView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 200
        }
    }

    override fun onStart() {
        super.onStart()

        if (currentUser == null) {
            goToLoginFragment()
        }
    }

    private fun goToNewMessageFragment() {
        val action =
            LatestMessagesFragmentDirections.actionLatestMessagesFragmentToNewMessageFragment()
        findNavController().navigate(action)
    }

    private fun goToLoginFragment() {
        val action = LatestMessagesFragmentDirections.actionLatestMessagesFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun goToChatFragment(user: User) {
        val action =
            LatestMessagesFragmentDirections.actionLatestMessagesFragmentToChatFragment(user)
        findNavController().navigate(action)
    }

    private fun goToProfileFragment() {
        val action =
            LatestMessagesFragmentDirections.actionLatestMessagesFragmentToProfileFragment()
        findNavController().navigate(action)
    }

    private fun goToProfileTutorFragment() {
        val action =
            LatestMessagesFragmentDirections.actionLatestMessagesFragmentToProfileTutor()
        findNavController().navigate(action)
    }

    private fun goToTutorListFragment() {
        val action =
            LatestMessagesFragmentDirections.actionLatestMessagesFragmentToTutorListFragment()
        findNavController().navigate(action)
    }

    override fun onLatestUserClickListener(latestUserMessage: LatestUserMessage) {
        goToChatFragment(latestUserMessage.user)
    }

    private fun startVideoCall() {
        val roomName = "${currentUsers?.id}"

        val userInfo = JitsiMeetUserInfo()
        userInfo.displayName = "${currentUsers?.username}"
        userInfo.email = "${currentUsers?.email}"
        currentUsers?.profileImgUrl?.let {userInfo.avatar = URL(currentUsers?.profileImgUrl)}

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