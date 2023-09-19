package com.hifi.redeal.memo.utils

import android.view.GestureDetector
import android.view.MotionEvent

class SwipeGestureListener(private val onSwipe: (direction: Direction) -> Unit) :
    GestureDetector.SimpleOnGestureListener() {

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1.x < e2.x) {
            // 슬라이드 우측 방향
            onSwipe(Direction.RIGHT)
        } else {
            // 슬라이드 좌측 방향
            onSwipe(Direction.LEFT)
        }
        return true
    }

    enum class Direction {
        LEFT,
        RIGHT
    }
}
