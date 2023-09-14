package com.hifi.redeal.schedule.model

data class ScheduleData(val clientIdx: Int, val isScheduleFinish: Boolean, val isVisitSchedule: Boolean, val scheduleContext: String,
val scheduleDataCreateTime: String, val scheduleDeadlineTime: String, val scheduleTitle: String)