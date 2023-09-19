package com.hifi.redeal.account.model

import com.google.firebase.Timestamp

data class ClientInputData(
    val clientAddress: String? = null,
    val clientCeoPhone: String? = null,
    val clientDetailAdd: String? = null,
    val clientExplain: String? = null,
    val clientFaxNumber: String? = null,
    val clientIdx: Long? = null,
    val clientManagerName: String? = null,
    val clientManagerPhone: String? = null,
    val clientMemo: String? = null,
    val clientName: String? = null,
    val clientState: Long? = null,
    @field:JvmField
    val isBookmark: Boolean? = null,
    val photoMemoIdxList: List<Long>? = null,
    val recordMemoIdxList: List<Long>? = null,
    val transactionIdxList: List<Long>? = null,
    val viewCount: Long? = null,
)

data class ClientData(
    val clientAddress: String? = null,
    val clientCeoPhone: String? = null,
    val clientDetailAdd: String? = null,
    val clientExplain: String? = null,
    val clientFaxNumber: String? = null,
    val clientIdx: Long? = null,
    val clientManagerName: String? = null,
    val clientManagerPhone: String? = null,
    val clientMemo: String? = null,
    var clientName: String? = null,
    var clientState: Long? = null,
    @field:JvmField
    var isBookmark: Boolean? = null,
    val photoMemoIdxList: List<Long>? = null,
    val recordMemoIdxList: List<Long>? = null,
    val transactionIdxList: List<Long>? = null,
    val viewCount: Long? = null,
    var recentContactDate: Timestamp? = null,
    var recentVisitDate: Timestamp? = null
)