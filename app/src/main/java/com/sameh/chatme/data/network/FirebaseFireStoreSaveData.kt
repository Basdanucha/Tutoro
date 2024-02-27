package com.sameh.chatme.data.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sameh.chatme.constants.Constants
import com.sameh.chatme.data.model.User
import com.sameh.chatme.ui.presenter.FirebaseFireStoreRegisterPresenter
import com.sameh.chatme.ui.presenter.FirebaseFireStoreSaveDataProfilePresenter
import javax.inject.Inject

class FirebaseFireStoreSaveData @Inject constructor(
    private val firebaseCollection: FirebaseFirestore
) {

    var firebaseFireStoreRegisterPresenter: FirebaseFireStoreRegisterPresenter? = null

    var firebaseFireStoreSaveDataProfilePresenter: FirebaseFireStoreSaveDataProfilePresenter? = null

    fun insertUserToFireStoreDB(
        username: String,
        email: String,
        uri: String?,
        imageId: String?,
        subject: String?,
        teachClass: String?,
        description : String? ,
        phonenumber : String? ,
        lineid : String?,
        videoUrl: String?,
        birthday: String?,
        videoId:String?
    ) {
        //firebaseCollection = Firebase.firestore
        val uid = FirebaseAuth.getInstance().uid ?: return
        val myRef = firebaseCollection.collection("users")
        //val user = User(username, email, uri)
        val user = User(uid, username, email, uri, imageId , subject ,teachClass,description,phonenumber,lineid,videoUrl,birthday,videoId)
        myRef.document(uid).set(user)
            .addOnSuccessListener {
                firebaseFireStoreRegisterPresenter!!.ifUserInsertedSuccess(
                    true,
                    Constants.SUCCESS_MESSAGE
                )
            }
            .addOnFailureListener {
                firebaseFireStoreRegisterPresenter!!.ifUserInsertedSuccess(
                    false,
                    it.message!!
                )
            }
    }

    fun updateUserInFireStoreDB(user: User) {
        val myRef = firebaseCollection.collection("users")

        myRef.document(user.id).update(
            "username", user.username,
            "profileImgUrl", user.profileImgUrl,
            "profileImageId", user.profileImageId,
            "subject", user.subject ,
            "teachClass",user.teachClass ,
            "description",user.description,
            "phonenumber",user.phonenumber,
            "lineid",user.lineid,
            "videoUrl",user.videoUrl,
            "birthday",user.birthday,
            "videoId",user.videoId

        ).addOnSuccessListener {
                firebaseFireStoreSaveDataProfilePresenter?.isUpdateUserFromFireStoreSuccess(
                    true,
                    Constants.SUCCESS_MESSAGE
                )
            }
            .addOnFailureListener {
                firebaseFireStoreSaveDataProfilePresenter?.isUpdateUserFromFireStoreSuccess(
                    false,
                    it.message!!
                )
            }
    }

}