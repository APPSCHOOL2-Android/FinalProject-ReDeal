package com.hifi.redeal.incoming

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.R

class CallNumberCheckWorker(context: Context, workerParams: WorkerParameters):
    Worker(context, workerParams) {

    companion object{
        lateinit var incommingNumber: String
    }

    val NOTIFICATION_CHANNEL1_ID = "CHANNEL_REDEAL1"
    val NOTIFICATION_CHANNEL1_NAME = "리딜"
    var notiId = 30

    override fun doWork(): Result {
        // 실행할 내용을 작성 한다.
        Log.d("ttt","전화번호: $incommingNumber")

        var clientName : String? = null
        var clientExplain : String? = null
        var clientManagerName : String? = null

        val uid = "1" // 추후 uid로 정보 찾음. 프리퍼런스를 이용.
        val db = Firebase.firestore

        // 대표 번호 조회
        db.collection("userData")
            .document(uid)
            .collection("clientData")
            .whereEqualTo("clientCeoPhone", incommingNumber)
            .get()
            .addOnCompleteListener {
                for(a1 in it.result){
                    clientName = a1["clientName"] as String
                    clientExplain = a1["clientExplain"] as String

                    val builder = getNotificationBuilder(NOTIFICATION_CHANNEL1_ID)
                    builder.setSmallIcon(R.drawable.notifications_24px)
                    builder.setContentTitle("${clientName}로 부터 전화")
                    builder.setContentText("거래처 : $clientName \n 한 줄 설명 : $clientExplain")

                    val notification = builder.build()
                    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(notiId, notification)
                    notiId++

                }
            }.addOnCompleteListener {
                db.collection("userData")
                    .document(uid)
                    .collection("clientData")
                    .whereEqualTo("clientManagerPhone", incommingNumber)
                    .get()
                    .addOnCompleteListener {
                        for(a1 in it.result){
                            clientName = a1["clientName"] as String
                            clientManagerName = a1["clientManagerName"] as String
                            val builder = getNotificationBuilder(NOTIFICATION_CHANNEL1_ID)
                            builder.setSmallIcon(R.drawable.notifications_24px)
                            builder.setContentTitle("${clientName}의 $clientManagerName")
                            builder.setContentText("거래처 : $clientName \n담당자 : $clientManagerName")
                            val notification = builder.build()
                            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(notiId, notification)
                            notiId++
                        }
                    }
            }

        return Result.success()
    }

    // Notification 메시지 관리 객체를 생성하는 메서드
    // Notification 채널 id를 받는다.
    fun getNotificationBuilder(id:String) : NotificationCompat.Builder{
        return NotificationCompat.Builder(applicationContext, id)
    }
}