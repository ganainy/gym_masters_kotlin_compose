package com.ganainy.gymmasterscompose.ui.theme

import android.text.TextUtils
import android.util.Patterns

object AppUtils {


    fun isValidEmail(email: String): Boolean {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
                )
    }

    fun isValidFieldLength(field: String, length: Int): Boolean {
        return (field.trim { it <= ' ' }
            .isEmpty()) || field.length >= length
    }


}