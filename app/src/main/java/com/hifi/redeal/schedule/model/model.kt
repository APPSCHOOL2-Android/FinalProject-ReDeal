package com.hifi.redeal.schedule.model

import com.google.firebase.Timestamp

// fb와 동일한 형태
data class ScheduleData(val clientIdx: Long, val isScheduleFinish: Boolean, val isVisitSchedule: Boolean, val scheduleContext: String,
val scheduleDataCreateTime: Timestamp, val scheduleDeadlineTime: Timestamp, val scheduleTitle: String)

// 뷰에 보여줄 정보를 가지고 있는 형태
data class ScheduleTotalData(val clientIdx: Long, val isScheduleFinish: Boolean, val isVisitSchedule: Boolean,val scheduleTitle: String, val scheduleContext: String,
                             val scheduleDataCreateTime: Timestamp, val scheduleDeadlineTime: Timestamp,
                             var clientName: String?, var clientManagerName: String?, var clientState: Long?
                             )

// 회사명, 담당자명, 거래상태