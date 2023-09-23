package com.hifi.redeal.account.repository.model

import com.google.firebase.Timestamp

data class NotificationData(
    val senderId: String? = null,
    val clientIdx: Long? = null,
    val notifyTime: Timestamp? = null,
    @JvmField
    val isChecked: Boolean? = null,
    var notificationId: String? = null
)