package com.hifi.redeal.memo.model

import android.net.Uri
import com.google.firebase.Timestamp

data class PhotoMemoData(val context:String, val date:Timestamp, val srcArr:List<String>)
data class UserPhotoMemoData(val clientIdx:Long, val context:String, val date:Timestamp, val srcArr:List<String>)
data class UserRecordMemoData(val clientIdx:Long, val context:String, val date:Timestamp, val audioSrc:Uri?, val audioFilename:String)
data class RecordMemoData(val context:String, val date:Timestamp, val audioSrc:Uri?, val audioFilename:String)