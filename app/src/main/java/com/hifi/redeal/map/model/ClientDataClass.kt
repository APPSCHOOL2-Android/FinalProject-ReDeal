package com.hifi.redeal.map.model

data class ClientDataClass(
    val clientIdx: Long,
    val clientName: String,
    val clientAddress: String?,
    val clientCeoPhone: String?,
    val clientDetailAdd: String?,
    val clientExplain: String?,
    val clientFaxNumber: String?,
    val clientManagerName: String?,
    val clientManagerPhone: String?,
    val clientMemo: String?,
    val clientState: Long?,
    val isBookmark: Boolean?,
    val photoMemoIdxList: List<Long>?,
    val recordMemoIdxList: List<Long>?,
    val transactionIdxList: List<Long>?,
    val viewCount: Long?
)