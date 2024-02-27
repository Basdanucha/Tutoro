package com.sameh.chatme.ui.fragments.TutorList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.sameh.chatme.data.model.User
import com.sameh.chatme.data.network.FirebaseAuthentication
import com.sameh.chatme.databinding.FragmentTutorListBinding
import com.sameh.chatme.ui.alertdialog.LoadingAlertDialog
import com.sameh.chatme.ui.fragments.TutorList.adapter.NewMessageAdapter
import com.sameh.chatme.ui.fragments.TutorList.adapter.OnUserClickListener
import com.sameh.chatme.ui.fragments.latestmessages.adapter.LatestUserAndMessagesAdapter
import com.sameh.chatme.ui.presenter.FirebaseRetrieveFromFireStoreNewMessagePresenter
import com.sameh.chatme.ui.viewModel.LatestMessageViewModel
import com.sameh.chatme.ui.viewModel.NewMessageViewModel
import com.sameh.chatme.ui.viewModel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import javax.inject.Inject

@AndroidEntryPoint
class TutorListFragment : Fragment(), FirebaseRetrieveFromFireStoreNewMessagePresenter,
    OnUserClickListener {

    private val latestMessageViewModel: LatestMessageViewModel by viewModels()
    lateinit var latestUserAndMessagesAdapter: LatestUserAndMessagesAdapter
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var binding: FragmentTutorListBinding

    private val newMessageViewModel: NewMessageViewModel by viewModels()

    @Inject
    lateinit var newMessageAdapter: NewMessageAdapter

    private lateinit var loadingAlertDialog: LoadingAlertDialog
    @Inject
    lateinit var auth: FirebaseAuth
    lateinit var fAuth : FirebaseAuthentication

    private var currentUser = Firebase.auth.currentUser
    private var currentUsers: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTutorListBinding.inflate(inflater, container, false)

        newMessageViewModel.repo.firebaseRetrieveFromFireStore.firebaseRetrieveFromFireStoreNewMessagePresenter =
            this

        loadingAlertDialog = LoadingAlertDialog(requireContext())
        latestMessageViewModel.getCurrentUser()
        profileViewModel.getCurrentUser()

        newMessageViewModel.getAllTutor()
        loadingAlertDialog.showLoadingAlertDialog()
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        profileViewModel.currentUserLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUsers = it
            }
            loadingAlertDialog.hideLoadingAlertDialog()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }


        binding.cirImgUser2.setOnClickListener {

            if (currentUsers?.subject != null){
            goToProfileTutor()
            }else{
            goToProfile()
            }
        }

        latestMessageViewModel.currentUserProfileImageLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.cirImgUser2.load(it)
            }
        }

        latestMessageViewModel.latestUserAndMessagesLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                latestUserAndMessagesAdapter.setData(it)
            }
        }


        newMessageViewModel.userLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                newMessageAdapter.setData(it)
            }
            loadingAlertDialog.hideLoadingAlertDialog()
        }

        binding.imgBtnBack2.setOnClickListener {
            goToLatestMessagesFragment()
        }

        binding.imgBtnLogout.setOnClickListener {
            goToLogin()
        }

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    newMessageViewModel.searchOnUsers(query)

                    newMessageViewModel.searchUsersLiveData.observe(viewLifecycleOwner) {
                        if (it != null) {
                            newMessageAdapter.setData(it as ArrayList<User>)
                        }
                    }
                } else {
                    newMessageViewModel.getAllUser()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    newMessageViewModel.searchOnUsers(newText)

                    newMessageViewModel.searchUsersLiveData.observe(viewLifecycleOwner) {
                        if (it != null) {
                            newMessageAdapter.setData(it as ArrayList<User>)
                        }
                    }
                } else {
                    newMessageViewModel.getAllUser()
                }
                return true
            }
        })

    }

    private fun setUpRecyclerView() {
        binding.usersRecView.adapter = newMessageAdapter
        newMessageAdapter.onClickListener = this
        binding.usersRecView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        binding.usersRecView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 200
        }
    }

    private fun goToLatestMessagesFragment() {
        val action = TutorListFragmentDirections.actionTutorListFragmentToLatestMessagesFragment()
        findNavController().navigate(action)
    }

    private fun goToLogin() {
        auth.signOut()
        if (currentUsers?.id != null) {
            deleteTokenFromFirebaseDatabase(currentUsers!!.id)
        }
        val action = TutorListFragmentDirections.actionTutorListFragmentToLogin()
        findNavController().navigate(action)
    }

    private fun goToProfile() {
        val action = TutorListFragmentDirections.actionTutorListFragmentToProfile()
        findNavController().navigate(action)
    }

    private fun goToProfileTutor() {
        val action = TutorListFragmentDirections.actionTutorListFragmentToProfileTutor()
        findNavController().navigate(action)
    }

    private fun goToChatFragment(user: User) {
        val action = TutorListFragmentDirections.actionTutorListFragmentToChatFragment(user)
        findNavController().navigate(action)
    }

    override fun ifRetrieveFromFirebaseSuccess(isSuccess: Boolean, state: String) {
        if (!isSuccess) {
            Toast.makeText(requireContext(), state, Toast.LENGTH_SHORT).show()
            loadingAlertDialog.hideLoadingAlertDialog()
        }
    }

    override fun onUserClickListener(user: User) {
        if (user.email == currentUser!!.email) {
            Toast.makeText(requireContext(), "You", Toast.LENGTH_SHORT).show()
            goToChatFragment(user)
        } else {
            goToChatFragment(user)
        }
    }

    fun deleteTokenFromFirebaseDatabase(userId:String) {
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            databaseReference.child(userId).child("fcmToken").removeValue()
                .addOnSuccessListener {
                    println("deleted token success")
                }
                .addOnFailureListener {
                    println("deleted token failed")
                }
        }
    }



}