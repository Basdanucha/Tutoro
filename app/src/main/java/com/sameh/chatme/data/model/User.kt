package com.sameh.chatme.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id: String = "",
    var username: String = "",
    var email: String = "",
    var profileImgUrl: String? = null,
    var profileImageId: String? = null,
    var subject : String? = null,
    var teachClass : String? = null,
    var description : String? = null,
    var phonenumber : String? = null,
    var lineid : String? = null,
    var videoUrl: String? = null,
    var birthday: String?= null,
    var videoId: String? = null,
): Parcelable