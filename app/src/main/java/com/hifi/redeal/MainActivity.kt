package com.hifi.redeal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.hifi.redeal.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding

    lateinit var navController: NavController

    val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    val mainBottomBarShowFragmentList = arrayOf(
        R.id.accountListFragment,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragmentMain) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener{ controller, destination, arguments ->
            activityMainBinding.bottomNavigationViewMain.isVisible = destination.id in mainBottomBarShowFragmentList
        }

//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (navController.previousBackStackEntry == null){
//                    finish()
//                    return
//                }
//
//                navController.popBackStack()
//                activityMainBinding.bottomNavigationViewMain.isVisible = navController.currentDestination?.id in mainBottomBarShowFragmentList
//            }
//        })
    }

    fun navigateTo(fragmentId: Int, bundle: Bundle? = null) {
        navController.navigate(fragmentId, bundle, navOptions)
    }

    fun intervalBetweenDateText(beforeDate: String): String {
        val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
        val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beforeDate)

        val diffMilliseconds = nowFormat.time - beforeFormat.time
        val diffSeconds = diffMilliseconds / 1000
        val diffMinutes = diffMilliseconds / (60 * 1000)
        val diffHours = diffMilliseconds / (60 * 60 * 1000)
        val diffDays = diffMilliseconds / (24 * 60 * 60 * 1000)

        val nowCalendar = Calendar.getInstance().apply { time = nowFormat }
        val beforeCalendar = Calendar.getInstance().apply { time = beforeFormat }

        val diffYears = nowCalendar.get(Calendar.YEAR) - beforeCalendar.get(Calendar.YEAR)
        var diffMonths = diffYears * 12 + nowCalendar.get(Calendar.MONTH) - beforeCalendar.get(
            Calendar.MONTH)
        if (nowCalendar.get(Calendar.DAY_OF_MONTH) < beforeCalendar.get(Calendar.DAY_OF_MONTH)) {
            diffMonths--
        }

        if (diffYears > 0) {
            return "${diffYears}년 전"
        }
        if (diffMonths > 0) {
            return "${diffMonths}개월 전"
        }
        if (diffDays > 0) {
            return "${diffDays}일 전"
        }
        if (diffHours > 0) {
            return "${diffHours}시간 전"
        }
        if (diffMinutes > 0) {
            return "${diffMinutes}분 전"
        }
        if (diffSeconds > 0) {
            return "${diffSeconds}초 전"
        }
        if(diffSeconds > -1){
            return "방금"
        }
        return ""
    }

    fun getTime(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return dateFormat.format(date)
    }
}