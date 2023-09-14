package com.hifi.redeal.schedule.model

import com.google.firebase.Timestamp

data class ScheduleData(val clientIdx: Long, val isScheduleFinish: Boolean, val isVisitSchedule: Boolean, val scheduleContext: String,
val scheduleDataCreateTime: Timestamp, val scheduleDeadlineTime: Timestamp, val scheduleTitle: String)