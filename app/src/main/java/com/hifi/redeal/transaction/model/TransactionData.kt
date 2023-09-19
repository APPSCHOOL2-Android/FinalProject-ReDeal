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
    var clientName: String?
)
