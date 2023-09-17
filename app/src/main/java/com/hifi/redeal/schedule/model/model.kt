package com.hifi.redeal.schedule.model

import com.google.firebase.Timestamp

// fb와 동일한 형태
data class ScheduleData(var scheduleIdx: Long, var clientIdx: Long, @JvmField var isScheduleFinish: Boolean, @JvmField var isVisitSchedule: Boolean, var scheduleContext: String,
                        var scheduleDataCreateTime: Timestamp, var scheduleDeadlineTime: Timestamp, var scheduleFinishTime: Timestamp, var scheduleTitle: String)

// 뷰에 보여줄 정보를 가지고 있는 형태
data class ScheduleTotalData(var scheduleIdx: Long, var clientIdx: Long, @JvmField var isScheduleFinish: Boolean, @JvmField var isVisitSchedule: Boolean,var scheduleTitle: String, var scheduleContext: String,
                             var scheduleDataCreateTime: Timestamp, var scheduleDeadlineTime: Timestamp,
                             var clientName: String?, var clientManagerName: String?, var clientState: Long?, @JvmField var isBookmark: Boolean?
                             )

data class ClientSimpleData(var clientIdx: Long, var clientName: String, var clientManagerName: String, var clientState: Long, @JvmField var isBookmark: Boolean)

data class ClientData(var clientIdx: Long, var clientName: String, var clientManagerName: String, var clientState: Long, @JvmField var isBookmark: Boolean,
var clientAddress: String,var clientCeoPhone: String,var clientDetailAdd: String,var clientExplain: String,var clientFaxNumber: String,var clientManagerPhone: String,var clientMemo: String)