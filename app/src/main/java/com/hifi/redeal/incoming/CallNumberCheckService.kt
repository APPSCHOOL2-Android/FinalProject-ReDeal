package com.hifi.redeal.incoming

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.R

class CallNumberCheckService: Service() {

    val NOTIFICATION_CHANNEL1_ID = "CHANNEL_REDEAL1"
    val NOTIFICATION_CHANNEL1_NAME = "리딜"
    var notiId = 30

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val incommingNumber = intent?.getStringExtra("incomingNumber")!!

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

        // 알림 설정 안드로이드 8.0 이상이라면..
        val builder = getNotificationBuilder(NOTIFICATION_CHANNEL1_ID)
        builder.setSmallIcon(android.R.drawable.ic_menu_search)
        builder.setContentTitle("연락처 확인 서비스 진행 중")
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val notification = builder.build()
        startForeground(notiId, notification)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    // Notification 메시지 관리 객체를 생성하는 메서드
    // Notification 채널 id를 받는다.
    fun getNotificationBuilder(id: String): NotificationCompat.Builder {
        // 안드로이드 8.0 이상이라면
        return NotificationCompat.Builder(this, id)
    }

}