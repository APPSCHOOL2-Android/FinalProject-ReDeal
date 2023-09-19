package com.hifi.redeal.account.model

import com.google.firebase.Timestamp

data class ScheduleData(
    val clientIdx: Long? = null,
    @field:JvmField
    val isScheduleFinish: Boolean? = null,
    @field:JvmField
    val isVisitSchedule: Boolean? = null,
    val scheduleContext: String? = null,
    val scheduleDataCreateTime: Timestamp? = null,
    val scheduleDeadlineTime: Timestamp? = null,
    val scheduleFinishTime: Timestamp? = null,
    val scheduleIdx: Long? = null,
    val scheduleTitle: String? = null
)
