package com.hifi.redeal


import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.hifi.redeal.auth.AuthFindPwFragment
import com.hifi.redeal.auth.AuthJoinFragment
import com.hifi.redeal.auth.AuthLoginFragment
import kotlin.concurrent.thread
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.transition.MaterialSharedAxis
import com.hifi.redeal.databinding.ActivityMainBinding
import com.hifi.redeal.map.view.MapFragment
import com.hifi.redeal.map.view.MapSearchRegionFragment
import androidx.lifecycle.ViewModelProvider
import com.hifi.redeal.databinding.ActivityMainBinding
import com.hifi.redeal.schedule.EditScheduleFragment
import com.hifi.redeal.schedule.MakeScheduleFragment
import com.hifi.redeal.schedule.ScheduleManageFragment
import com.hifi.redeal.schedule.ScheduleSelectByClientFragment
import com.hifi.redeal.schedule.UnvisitedScheduleFragment
import com.hifi.redeal.schedule.VisitedScheduleFragment
import com.hifi.redeal.schedule.vm.ScheduleVM
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding
    lateinit var scheduleVM: ScheduleVM

    var newFragment:Fragment? = null
    var oldFragment:Fragment? = null

    val permissionList = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.USE_FULL_SCREEN_INTENT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )
    val NOTIFICATION_CHANNEL1_ID = "CHANNEL_REDEAL1"
    val NOTIFICATION_CHANNEL1_NAME = "리딜"
    companion object{
        val SCHEDULE_MANAGE_FRAGMENT = "ScheduleManageFragment"
        val UNVISITED_SCHEDULE_FRAGMENT = "UnvisitedScheduleFragment"
        val VISITED_SCHEDULE_FRAGMENT = "VisitedScheduleFragment"
        val MAKE_SCHEDULE_FRAGMENT = "MakeScheduleFragment"
        val SCHEDULE_SELECT_BY_CLIENT_FRAGMENT = "ScheduleSelectByClientFragment"
        val EDIT_SCHEDULE_FRAGMENT = "EditScheduleFragment"
        val AUTH_LOGIN_FRAGMENT = "AuthLoginFragment"
        val AUTH_JOIN_FRAGMENT = "AuthJoinFragment"
        val AUTH_FIND_PW_FRAGMENT = "AuthFindPwFragment"
        val MAKE_SCHEDULE_FRAGMENT = "MakeScheduleFragment"
        val MAP_FRAGMENT = "MapFragment"
        val MAP_SEARCH_REGION_FRAGMENT = "MapSearchRegionFragment"
      
        const val BASE_URL = "https://dapi.kakao.com/"
    }

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

        requestPermissions(permissionList, 10)
        addNotificationChannel(NOTIFICATION_CHANNEL1_ID, NOTIFICATION_CHANNEL1_NAME)
        scheduleVM = ViewModelProvider(this)[ScheduleVM::class.java]

    }

    fun replaceFragment(name: String, addToBackStack: Boolean, bundle: Bundle?) {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragmentMain) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener{ controller, destination, arguments ->
            activityMainBinding.bottomNavigationViewMain.isVisible = destination.id in mainBottomBarShowFragmentList
        }

    }

    // 지정한 Fragment를 보여주는 메서드
    fun replaceFragment(name:String, addToBackStack:Boolean, bundle:Bundle?){

        SystemClock.sleep(200)

        // Fragment 교체 상태로 설정한다.
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // newFragment 에 Fragment가 들어있으면 oldFragment에 넣어준다.

        if(newFragment != null){
            oldFragment = newFragment
        }

        // 새로운 Fragment를 담을 변수
        newFragment = when(name){
            SCHEDULE_MANAGE_FRAGMENT -> ScheduleManageFragment()
            UNVISITED_SCHEDULE_FRAGMENT -> UnvisitedScheduleFragment()
            VISITED_SCHEDULE_FRAGMENT -> VisitedScheduleFragment()
            MAKE_SCHEDULE_FRAGMENT -> MakeScheduleFragment()
            SCHEDULE_SELECT_BY_CLIENT_FRAGMENT -> ScheduleSelectByClientFragment()
            EDIT_SCHEDULE_FRAGMENT -> EditScheduleFragment()
            AUTH_LOGIN_FRAGMENT -> AuthLoginFragment()
            AUTH_JOIN_FRAGMENT -> AuthJoinFragment()
            AUTH_FIND_PW_FRAGMENT -> AuthFindPwFragment()
            MAKE_SCHEDULE_FRAGMENT -> MakeScheduleFragment()
            MAP_FRAGMENT -> MapFragment()
            MAP_SEARCH_REGION_FRAGMENT -> MapSearchRegionFragment()

            else -> Fragment()
        }

        newFragment?.arguments = bundle

        if(newFragment != null) {
            // 애니메이션 설정
            if(oldFragment != null){
                oldFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                oldFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                oldFragment?.enterTransition = null
                oldFragment?.returnTransition = null
            }

            newFragment?.exitTransition = null
            newFragment?.reenterTransition = null
            newFragment?.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            newFragment?.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)


            // Fragment를 교채한다.
            fragmentTransaction.replace(R.id.mainContainer, newFragment!!)

            if (addToBackStack == true) {
                // Fragment를 Backstack에 넣어 이전으로 돌아가는 기능이 동작할 수 있도록 한다.
                fragmentTransaction.addToBackStack(name)
            }

            // 교체 명령이 동작하도록 한다.
            fragmentTransaction.commit()
        }
    }

    // Fragment를 BackStack에서 제거한다.
    fun removeFragment(name:String){
        supportFragmentManager.popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    
    // 입력 요소에 포커스를 주는 메서드
    fun showSoftInput(view: View) {
        view.requestFocus()

        val inputMethodManger = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        thread {
            SystemClock.sleep(200)
            inputMethodManger.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }


    // Notification Channel을 등록하는 메서드
    // 첫 번째 : 코드에서 채널을 관리하기 위한 이름
    // 두 번째 : 사용자에게 노출 시킬 이름
    fun addNotificationChannel(id: String, name:String){
        // 안드로이드 8.0 이상일 때만 동작하게 한다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // 알림 메시지를 관리하는 객체를 추출한다.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // id를 통해 NotificationChannel 객체를 추출한다.
            // 채널이 등록된 적이 없다면 null을 반환한다.
            val channel = notificationManager.getNotificationChannel(id)
            // 채널이 등록된 적이 없다면...
            if(channel == null){
                // 채널 객체를 생성한다.
                val newChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
                // 단말기에 LED 램프가 있다면 램프를 사용하도록 설정한다.
                newChannel.enableLights(true)
                // LED 램프의 색상을 설정한다.
                newChannel.lightColor = Color.BLUE
                // 진동을 사용할 것인가?
                newChannel.enableVibration(true)
                // 채널을 등록한다.
                notificationManager.createNotificationChannel(newChannel)
            }

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