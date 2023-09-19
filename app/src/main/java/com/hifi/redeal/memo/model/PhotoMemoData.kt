package com.hifi.redeal.memo.model

data class PhotoMemoData(val context:String, val date:Long, val srcArr:List<String>)
data class ClientData(val idx:Long, val name:String, val managerName:String)