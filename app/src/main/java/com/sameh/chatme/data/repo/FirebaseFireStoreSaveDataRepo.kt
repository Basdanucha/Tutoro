package com.sameh.chatme.data.repo

import com.sameh.chatme.data.model.User

interface FirebaseFireStoreSaveDataRepo {

    suspend fun insertUserToFireStoreDB(username: String,
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
                                        videoId: String?)

    suspend fun updateUserInFireStoreDB(user: User)

}