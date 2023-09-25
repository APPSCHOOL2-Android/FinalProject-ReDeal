package com.hifi.redeal.map.model

data class ClientDataClass(
    val clientIdx: Long = 0,
    val clientName: String = "",
    val clientAddress: String = "",
    val clientCeoPhone: String = "",
    val clientDetailAdd: String = "",
    val clientExplain: String = "",
    val clientFaxNumber: String = "",
    val clientManagerName: String = "",
    val clientManagerPhone: String = "",
    val clientMemo: String? = "",
    val clientState: Long? = 0,

    @field:JvmField
    val isBookmark: Boolean? = false,

    val photoMemoIdxList: List<Long> = emptyList(),
    val recordMemoIdxList: List<Long> = emptyList(),
    val transactionIdxList: List<Long> = emptyList(),
    val viewCount: Long? = 0
)
