package com.hifi.redeal.transaction.model

import com.google.firebase.Timestamp


data class TransactionData(
    var clientIdx: Long,
    var date: Timestamp,
    @JvmField
    var isDeposit: Boolean,
    var transactionAmountReceived: String,
    var transactionIdx: Long,
    var transactionItemCount: Long,
    var transactionItemPrice: String,
    var transactionName: String,
)

data class customTransactionData(
    var clientIdx: Long,
    var date: Timestamp,
    @JvmField
    var isDeposit: Boolean,
    var transactionAmountReceived: String,
    var transactionIdx: Long,
    var transactionItemCount: Long,
    var transactionItemPrice: String,
    var transactionName: String,
    var clientName: String?
)

data class ClientData(
    var clientAddress: String? = null,
    var clientCeoPhone: String? = null,
    var clientDetailAdd: String? = null,
    var clientExplain: String? = null,
    var clientFaxNumber: String? = null,
    var clientIdx: Long? = null,
    var clientManagerName: String? = null,
    var clientManagerPhone: String? = null,
    var clientMemo: String? = null,
    var clientName: String? = null,
    var clientState: Long? = null,
    @field:JvmField
    var isBookmark: Boolean? = null,
    var photoMemoIdxList: List<Long>? = null,
    var recordMemoIdxList: List<Long>? = null,
    var transactionIdxList: List<Long>? = null,
    var viewCount: Long? = null,
)

data class ClientSimpleData(var clientIdx: Long,
                            var clientName: String,
                            var clientManagerName: String,
                            var clientState: Long,
                            @JvmField var isBookmark: Boolean)
