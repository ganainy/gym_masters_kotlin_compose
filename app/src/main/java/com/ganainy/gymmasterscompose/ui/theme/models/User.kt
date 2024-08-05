package com.ganainy.gymmasterscompose.ui.theme.models

import android.os.Parcel
import android.os.Parcelable

data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var about_me: String? = null,
    var photo: String? = null,
    var followers: Long? = null,
    var following: Long? = null,
    var rating: Long? = null
)


