package com.hifi.redeal.memo.model

import android.net.Uri

data class PhotoMemoData(val context:String, val date:Long, val srcArr:List<String>)
data class RecordMemoData(val clientIdx:Long?, val context:String, val date:Long, val audioSrc:String, val audioFilename:String,
    val audioFileUri:Uri?)
data class ClientData(val idx:Long, val name:String, val managerName:String)