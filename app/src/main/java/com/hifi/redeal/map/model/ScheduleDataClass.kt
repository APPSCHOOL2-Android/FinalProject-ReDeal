package com.hifi.redeal.map.model

import java.util.Date

data class ScheduleDataClass(
    val clientIdx: Long = 0,

    @field:JvmField
    val isScheduleFinish: Boolean = false,

    @field:JvmField
    val isVisitSchedule: Boolean = false,

    val scheduleContext: String = "",
    val scheduleDataCreateTime: Date = Date(),
    val scheduleDeadLineTime: Date = Date(),
    val scheduleFinishTime: Date = Date(),
    val scheduleIdx: Long = 0,
    val scheduleTitle: String = ""
)