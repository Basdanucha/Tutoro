package com.sameh.chatme.ui.fragments.profile

import OnVideoUploadListener
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.sameh.chatme.R
import com.sameh.chatme.constants.Constants
import com.sameh.chatme.data.model.User
import com.sameh.chatme.databinding.FragmentVideoBinding
import com.sameh.chatme.ui.alertdialog.LoadingAlertDialog
import com.sameh.chatme.ui.fragments.chat.TutorDetail
import com.sameh.chatme.ui.presenter.FirebaseFireStoreSaveDataProfilePresenter
import com.sameh.chatme.ui.presenter.FirebaseStorageProfilePresenter
import com.sameh.chatme.ui.viewModel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException

@AndroidEntryPoint
class VideoFragment : Fragment(), FirebaseStorageProfilePresenter,
    FirebaseFireStoreSaveDataProfilePresenter {

    private lateinit var binding: FragmentVideoBinding


    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var loadingAlertDialog: LoadingAlertDialog

    private var currentUser: User? = null

    private var selectedUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)

        loadingAlertDialog = LoadingAlertDialog(requireContext())

        profileViewModel.repo.firebaseStorageProfile.firebaseStorageProfilePresenter = this
        profileViewModel.repo.firebaseFireStoreSaveData.firebaseFireStoreSaveDataProfilePresenter = this
        profileViewModel.getCurrentUser()

        loadingAlertDialog.showLoadingAlertDialog()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         useYouTubePlayerAPI("https://studio.youtube.com/channel/UCrHNB_uNJANgSU9rOq5wOMw/videos/upload?d=ud&filter=%5B%5D&sort=%7B%22columnType%22%3A%22date%22%2C%22sortOrder%22%3A%22DESCENDING%22%7D")

       // binding.wevView.loadUrl("https://www.youtube.com/watch?v=4as2bcQOj00")

        // this will enable the javascript settings, it can also allow xss vulnerabilities
    //    binding.wevView.settings.javaScriptEnabled = true

        // if you want to enable zoom feature
    //    binding.wevView.settings.setSupportZoom(true)


        profileViewModel.currentUserLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUser = it
                setUserDataInUi()
            }
            loadingAlertDialog.hideLoadingAlertDialog()
        }


        binding.btnSave.setOnClickListener {

            val isNotYoutubeUrl = !binding.VideoUrl.text.toString().contains("youtube")
            val isNotYoutubeUrl2 = !binding.VideoUrl.text.toString().contains("youtu.be")

            if (isNotYoutubeUrl && isNotYoutubeUrl2){
                Toast.makeText(requireContext(), "please input youtube url", Toast.LENGTH_SHORT)
                    .show()

            }else {
                updateUser(null, null)
                useYouTubePlayerAPI(currentUser?.videoUrl ?: "")

                if (selectedUri != null) {
                    profileViewModel.uploadPhotoToFirebaseStorageProfile(selectedUri!!)
                    loadingAlertDialog.showLoadingAlertDialog()
                } else {
                    updateUser(null, null)
                }
            }

        }

//        binding.videoView.setOnLongClickListener {
//            confirmDeleteProfileImage()
//            true
//        }

        binding.imgBtnBack.setOnClickListener {
            goToProfileTutorFragment()
        }

        binding.btnRegisTutor.setOnClickListener {
            confirmDeleteProfileImage()
        }



    }

    private fun setUserDataInUi() {
        if (currentUser?.videoUrl != null) {
             useYouTubePlayerAPI(currentUser?.videoUrl?:"")
            binding.VideoUrl.setText(currentUser?.videoUrl)
         //   setVideoView(currentUser?.videoUrl)
        }
      //  binding.tvEmail.text = "Video ID: "+currentUser?.videoId
    }


    private fun goToProfileTutorFragment() {
        val action = VideoFragmentDirections.actionVideoFragmentToProfileTutor()
        findNavController().navigate(action)
    }

    private fun openPhotoGalleryToSelectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        resultLauncherOfSelectPhoto.launch(intent)
    }

    private var resultLauncherOfSelectPhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri = intent!!.data

               // binding.videoView.setVideoURI(uri)
                selectedUri = uri

            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

    private fun updateUser(uri: Uri?, imageId: String?  ) {
        if (uri != null && imageId != null) {
            val id = currentUser!!.id
            val username = currentUser!!.username
            val email = currentUser!!.email
            val subject = currentUser!!.subject
            val teachClass = currentUser!!.teachClass
            val profileImage = currentUser!!.profileImgUrl
            val profileImageId = currentUser!!.profileImageId
            val description = currentUser!!.description
            val phonenumber = currentUser!!.phonenumber
            val lineid = currentUser!!.lineid
            val videoUrl = binding.VideoUrl.text.toString()
            val birthday = currentUser!!.birthday
            val user = User(id, username, email, profileImage, profileImageId , subject ,teachClass,description,phonenumber,lineid,videoUrl,birthday,imageId)
            profileViewModel.updateUserInFireStoreDB(user)
        } else {
            val id = currentUser!!.id
            val username = currentUser!!.username
            val email = currentUser!!.email
            val subject = currentUser!!.subject
            val teachClass = currentUser!!.teachClass
            val description = currentUser!!.description
            val phonenumber = currentUser!!.phonenumber
            val lineid = currentUser!!.lineid
            val videoUrl = binding.VideoUrl.text.toString()
            val videoId = currentUser!!.videoId
            val birthday = currentUser!!.birthday
            val profileImage = currentUser!!.profileImgUrl
            val profileImageId = currentUser!!.profileImageId
            val user = User(id, username, email, profileImage, profileImageId, subject, teachClass,description,phonenumber,lineid, videoUrl,birthday,videoId)
            profileViewModel.updateUserInFireStoreDB(user)
        }
    }

    private fun confirmDeleteProfileImage() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Video")
        builder.setMessage("Are you sure to delete your Video ?")
        builder.setPositiveButton("Yes") { _, _ ->
            deleteProfileImage()
            useYouTubePlayerAPI("https://studio.youtube.com/channel/UCrHNB_uNJANgSU9rOq5wOMw/videos/upload?d=ud&filter=%5B%5D&sort=%7B%22columnType%22%3A%22date%22%2C%22sortOrder%22%3A%22DESCENDING%22%7D")

        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.show()
    }

    private fun deleteProfileImage() {
        if (currentUser!!.videoUrl != null) {
            loadingAlertDialog.showLoadingAlertDialog()
            profileViewModel.deleteImageFromFireStorageProfile(currentUser!!.videoUrl!!)
            profileViewModel.updateUserInFireStoreDB(
                user = User(
                    currentUser!!.id,
                    currentUser!!.username,
                    currentUser!!.email,
                    currentUser!!.profileImgUrl,
                    currentUser!!.profileImageId,
                    currentUser!!.subject,
                    currentUser!!.teachClass,
                    currentUser!!.description,
                    currentUser!!.phonenumber,
                    currentUser!!.lineid,
                    null,
                    currentUser!!.birthday,
                    null,

                    )
            )
            binding.VideoUrl.text = null
        } else {
            Toast.makeText(requireContext(), "You don't have Video", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun isUploadPhotoSuccessful(
        isSuccess: Boolean,
        statue: String,
        uri: Uri?,
        imageId: String?
    ) {
        if (isSuccess) {
            if (currentUser?.videoUrl != null) {
                profileViewModel.deleteImageFromFireStorageProfile(currentUser!!.videoId!!)
            }
            updateUser(uri!!, imageId)
        } else {
            Toast.makeText(requireContext(), statue, Toast.LENGTH_SHORT).show()
        }
    }

    override fun isUpdateUserFromFireStoreSuccess(isSuccess: Boolean, state: String) {
        loadingAlertDialog.hideLoadingAlertDialog()
        if (isSuccess) {
            Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), state, Toast.LENGTH_SHORT).show()
        }
        profileViewModel.getCurrentUser()
    }

    private fun useYouTubePlayerAPI(state: String) {
        val webView = WebView(requireContext())
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(state)
        binding.wevView.addView(webView)
    }

//    private fun setVideoView(uri : String?) {
//        val mediaController = MediaController(requireContext())
//        mediaController.setAnchorView(binding.videoView)
//
//        binding.videoView.setMediaController(mediaController)
//        binding.videoView.setVideoPath(uri)
//        binding.videoView.requestFocus()
//        binding.videoView.setOnPreparedListener(MediaPlayer.OnPreparedListener {
//            binding.videoView.pause()
//        })
//    }


}

/*
fun reloadFragment() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        parentFragmentManager.beginTransaction().detach(this).commitNow()
        parentFragmentManager.beginTransaction().attach(this).commitNow()
        //requireFragmentManager().beginTransaction().attach(this).commitNow()
    } else {
        parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
    }
}
 */