package com.hifi.redeal.account.model

import com.google.firebase.Timestamp

data class ContactData(
    val clientIdx: Long? = null,
    val contactDate: Timestamp? = null,
    val contactIdx: Long? = null,
    val contactType: Long? = null
)
