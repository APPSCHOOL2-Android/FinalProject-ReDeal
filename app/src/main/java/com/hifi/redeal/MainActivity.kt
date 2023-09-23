package com.hifi.redeal


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialSharedAxis
import com.hifi.redeal.account.AccountDetailFragment
import com.hifi.redeal.account.AccountEditFragment
import com.hifi.redeal.account.AccountListFragment
import com.hifi.redeal.account.AddressSearchFragment
import com.hifi.redeal.auth.AuthFindPwFragment
import com.hifi.redeal.auth.AuthJoinFragment
import com.hifi.redeal.auth.AuthLoginFragment
import com.hifi.redeal.databinding.ActivityMainBinding
import com.hifi.redeal.map.view.MapFragment
import com.hifi.redeal.map.view.MapSearchRegionFragment
import com.hifi.redeal.memo.AddPhotoMemoFragment
import com.hifi.redeal.memo.AddRecordMemoFragment
import com.hifi.redeal.memo.MemoFragment
import com.hifi.redeal.memo.PhotoDetailFragment
import com.hifi.redeal.memo.PhotoMemoFragment
import com.hifi.redeal.memo.RecordMemoFragment
import com.hifi.redeal.myPage.MyPageEditNameFragment
import com.hifi.redeal.myPage.MyPageFragment
import com.hifi.redeal.schedule.view.EditScheduleFragment
import com.hifi.redeal.schedule.view.MakeScheduleFragment
import com.hifi.redeal.schedule.view.ScheduleManageFragment
import com.hifi.redeal.schedule.view.ScheduleSelectByClientFragment
import com.hifi.redeal.schedule.view.UnvisitedScheduleFragment
import com.hifi.redeal.schedule.view.VisitedScheduleFragment
import com.hifi.redeal.schedule.vm.ScheduleVM
import com.hifi.redeal.transaction.TransactionFragment
import com.skt.tmap.TMapTapi
import com.skt.tmap.TMapTapi.OnAuthenticationListenerCallback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding
    lateinit var scheduleVM: ScheduleVM

    lateinit var tMapTapi: TMapTapi

    var newFragment:Fragment? = null
    var oldFragment:Fragment? = null

    var uid = ""

    val permissionList = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.USE_FULL_SCREEN_INTENT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_MEDIA_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CALL_PHONE
    )
    val NOTIFICATION_CHANNEL1_ID = "CHANNEL_REDEAL1"
    val NOTIFICATION_CHANNEL1_NAME = "리딜"

    companion object{
        val ACCOUNT_LIST_FRAGMENT = "AccountListFragment"
        val ACCOUNT_DETAIL_FRAGMENT = "AccountDetailFragment"
        val ACCOUNT_EDIT_FRAGMENT = "AccountEditFragment"
        val ADDRESS_SEARCH_FRAGMENT = "AddressSearchFragment"
        val PHOTO_MEMO_FRAGMENT = "PhotoMemoFragment"
        val RECORD_MEMO_FRAGMENT = "RecrodMemoFragment"
        val MEMO_FRAGMENT = "MemoFragment"
        val ADD_PHOTO_MEMO_FRAGMENT = "AddPhotoMemoFragment"
        val ADD_RECORD_MEMO_FRAGMENT = "AddRecordMemoFragment"
        val PHOTO_DETAIL_FRAGMENT = "PhotoDetailFragment"
        val SCHEDULE_MANAGE_FRAGMENT = "ScheduleManageFragment"
        val UNVISITED_SCHEDULE_FRAGMENT = "UnvisitedScheduleFragment"
        val VISITED_SCHEDULE_FRAGMENT = "VisitedScheduleFragment"
        val MAKE_SCHEDULE_FRAGMENT = "MakeScheduleFragment"
        val SCHEDULE_SELECT_BY_CLIENT_FRAGMENT = "ScheduleSelectByClientFragment"
        val EDIT_SCHEDULE_FRAGMENT = "EditScheduleFragment"
        val AUTH_LOGIN_FRAGMENT = "AuthLoginFragment"
        val AUTH_JOIN_FRAGMENT = "AuthJoinFragment"
        val AUTH_FIND_PW_FRAGMENT = "AuthFindPwFragment"
        val MAP_FRAGMENT = "MapFragment"
        val MAP_SEARCH_REGION_FRAGMENT = "MapSearchRegionFragment"
        val TRANSACTION_FRAGMENT = "TransactionFragment"
        val MY_PAGE_FRAGMENT = "MyPageFragment"
        val MY_PAGE_EDIT_NAME_FRAGMENT = "MyPageEditNameFragment"

        const val BASE_URL = "https://dapi.kakao.com/"
        const val REGION_BASE_URL = "http://api.vworld.kr/"
    }

    val REQUEST_INTENTS = listOf(
        Intent.ACTION_GET_CONTENT,
        MediaStore.Audio.Media.RECORD_SOUND_ACTION
    )

//    lateinit var navController: NavController
//
//    val navOptions = NavOptions.Builder()
//        .setEnterAnim(R.anim.slide_in_right)
//        .setExitAnim(R.anim.slide_out_left)
//        .setPopEnterAnim(R.anim.slide_in_left)
//        .setPopExitAnim(R.anim.slide_out_right)
//        .build()

//    val mainBottomBarShowFragmentList = arrayOf(
//        R.id.accountListFragment,
//        R.id.scheduleManageFragment,
//        R.id.unvisitedScheduleFragment,
//        R.id.visitedScheduleFragment,
//        R.id.mapFragment,
//    )

    override fun onResume() {
        super.onResume()
        if(intent.getLongExtra("notifyClientIdx", -1) != -1L){
            Log.d("RedealNotify", "메인 액티비티 실행 : ${intent.getLongExtra("notifyClientIdx", -1)}")
            val bundle = Bundle()
            val notifyClientIdx = intent.getLongExtra("notifyClientIdx", -1)
            if(notifyClientIdx != -1L){
                bundle.putLong("notifyClientIdx", notifyClientIdx)
                replaceFragment(ACCOUNT_DETAIL_FRAGMENT, false, bundle)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        tMapTapi = TMapTapi(this)

        tMapTapi.setOnAuthenticationListenerCallback( object : OnAuthenticationListenerCallback {
            override fun SKTMapApikeySucceed() {
                Log.d("brudenell", "성공")
            }

            override fun SKTMapApikeyFailed(p0: String?) {
                Log.d("brudenell", "실패")
            }
        })

        tMapTapi.setSKTmapAuthentication(BuildConfig.TMAP_APP_KEY)

        requestPermissions(permissionList, 10)
        addNotificationChannel(NOTIFICATION_CHANNEL1_ID, NOTIFICATION_CHANNEL1_NAME)
        scheduleVM = ViewModelProvider(this)[ScheduleVM::class.java]

        activityMainBinding.run {

            bottomNavigationViewMain.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.accountListFragment -> {
                        replaceFragment(ACCOUNT_LIST_FRAGMENT, true)
                    }
                    R.id.scheduleManageFragment -> {
                        replaceFragment(SCHEDULE_MANAGE_FRAGMENT, true)
                    }
                    R.id.mapFragment -> {
                        replaceFragment(MAP_FRAGMENT, true)
                    }
                    R.id.transactionFragment -> {
                        replaceFragment(TRANSACTION_FRAGMENT, true)
                    }
                    R.id.memoFragment -> {
                        replaceFragment(MEMO_FRAGMENT, true)
                    }
                }
                true
            }
        }
        //replaceFragment(MAP_FRAGMENT,false,null)

        supportFragmentManager.addOnBackStackChangedListener {
            for (fragment in supportFragmentManager.fragments) {
                if (fragment.isVisible) {
                    activityMainBinding.bottomNavigationViewMain.isVisible = (fragment is AccountListFragment
                            || fragment is ScheduleManageFragment
                            || fragment is UnvisitedScheduleFragment
                            || fragment is VisitedScheduleFragment
                            || fragment is MapFragment
                            || fragment is MemoFragment)

                    when (fragment) {
                        is AccountListFragment -> {
                            activityMainBinding.bottomNavigationViewMain.run {
                                menu.forEach {
                                    if (it.itemId == R.id.accountListFragment) {
                                        it.isChecked = true
                                    }
                                }
                            }
                        }
                        is ScheduleManageFragment -> {
                            activityMainBinding.bottomNavigationViewMain.run {
                                menu.forEach {
                                    if (it.itemId == R.id.scheduleManageFragment) {
                                        it.isChecked = true
                                    }
                                }
                            }
                        }
                        is MapFragment -> {
                            activityMainBinding.bottomNavigationViewMain.run {
                                menu.forEach {
                                    if (it.itemId == R.id.mapFragment) {
                                        it.isChecked = true
                                    }
                                }
                            }
                        }
                        is TransactionFragment -> {
                            activityMainBinding.bottomNavigationViewMain.run {
                                menu.forEach {
                                    if (it.itemId == R.id.transactionFragment) {
                                        it.isChecked = true
                                    }
                                }
                            }
                        }
                        is MemoFragment -> {
                            activityMainBinding.bottomNavigationViewMain.run {
                                menu.forEach {
                                    if (it.itemId == R.id.memoFragment) {
                                        it.isChecked = true
                                    }
                                }
                            }
                        }
                    }
                    return@addOnBackStackChangedListener
                }
            }
        }

        replaceFragment(AUTH_LOGIN_FRAGMENT, false)

//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.navHostFragmentMain) as NavHostFragment
//        navController = navHostFragment.navController
//
//        navController.addOnDestinationChangedListener{ controller, destination, arguments ->
//            activityMainBinding.bottomNavigationViewMain.isVisible = destination.id in mainBottomBarShowFragmentList
//        }
//
//        activityMainBinding.run {
//            bottomNavigationViewMain.setupWithNavController(navController)
//        }
    }

//    fun replaceFragment2(name:String, addToBackStack:Boolean, bundle:Bundle? = null) {
//        val fragmentId = when(name){
//            ACCOUNT_LIST_FRAGMENT -> R.id.accountListFragment
//            ACCOUNT_DETAIL_FRAGMENT -> R.id.accountDetailFragment
//            ACCOUNT_EDIT_FRAGMENT -> R.id.accountEditFragment
//            ADDRESS_SEARCH_FRAGMENT -> R.id.addressSearchFragment
//            SELECT_FRAGMENT -> R.id.selectFragment
//            PHOTO_MEMO_FRAGMENT -> R.id.photoMemoFragment
//            ADD_PHOTO_MEMO_FRAGMENT -> R.id.addPhotoMemoFragment
//            PHOTO_DETAIL_FRAGMENT -> R.id.photoDetailFragment
//            RECORD_MEMO_FRAGMENT -> R.id.recordMemoFragment
//            ADD_RECORD_MEMO_FRAGMENT -> R.id.addRecordMemoFragment
//            SCHEDULE_MANAGE_FRAGMENT -> R.id.scheduleManageFragment
//            UNVISITED_SCHEDULE_FRAGMENT -> R.id.unvisitedScheduleFragment
//            VISITED_SCHEDULE_FRAGMENT -> R.id.visitedScheduleFragment
//            MAKE_SCHEDULE_FRAGMENT -> R.id.makeScheduleFragment
//            SCHEDULE_SELECT_BY_CLIENT_FRAGMENT -> R.id.scheduleSelectByClientFragment
//            EDIT_SCHEDULE_FRAGMENT -> R.id.editScheduleFragment
//            AUTH_LOGIN_FRAGMENT -> R.id.authLoginFragment
//            AUTH_JOIN_FRAGMENT -> R.id.authJoinFragment
//            AUTH_FIND_PW_FRAGMENT -> R.id.authFindPwFragment
//            MAP_FRAGMENT -> R.id.mapFragment
//            MAP_SEARCH_REGION_FRAGMENT -> R.id.mapSearchRegionFragment
//            else -> R.id.accountListFragment
//        }
//
//        if (!addToBackStack) {
//            val popUpFragmentId = navController.currentDestination?.id ?: R.id.authLoginFragment
//
//            val popUpNavOptions = NavOptions.Builder()
//                .setPopUpTo(popUpFragmentId, true)
//                .build()
//
//            navController.navigate(fragmentId, bundle, popUpNavOptions)
//        } else {
//            navigateTo(fragmentId, bundle)
//        }
//    }

    // 지정한 Fragment를 보여주는 메서드
    fun replaceFragment(name:String, addToBackStack:Boolean, bundle:Bundle? = null){

        SystemClock.sleep(200)

        // Fragment 교체 상태로 설정한다.
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // newFragment 에 Fragment가 들어있으면 oldFragment에 넣어준다.

        if(newFragment != null){
            oldFragment = newFragment
        }

        // 새로운 Fragment를 담을 변수
        newFragment = when(name){
            ACCOUNT_LIST_FRAGMENT -> AccountListFragment()
            ACCOUNT_DETAIL_FRAGMENT -> AccountDetailFragment()
            ACCOUNT_EDIT_FRAGMENT -> AccountEditFragment()
            ADDRESS_SEARCH_FRAGMENT -> AddressSearchFragment()
            MEMO_FRAGMENT -> MemoFragment()
            PHOTO_MEMO_FRAGMENT -> PhotoMemoFragment()
            ADD_PHOTO_MEMO_FRAGMENT -> AddPhotoMemoFragment()
            PHOTO_DETAIL_FRAGMENT -> PhotoDetailFragment()
            RECORD_MEMO_FRAGMENT -> RecordMemoFragment()
            ADD_RECORD_MEMO_FRAGMENT -> AddRecordMemoFragment()
            SCHEDULE_MANAGE_FRAGMENT -> ScheduleManageFragment()
            UNVISITED_SCHEDULE_FRAGMENT -> UnvisitedScheduleFragment()
            VISITED_SCHEDULE_FRAGMENT -> VisitedScheduleFragment()
            MAKE_SCHEDULE_FRAGMENT -> MakeScheduleFragment()
            SCHEDULE_SELECT_BY_CLIENT_FRAGMENT -> ScheduleSelectByClientFragment()
            EDIT_SCHEDULE_FRAGMENT -> EditScheduleFragment()
            AUTH_LOGIN_FRAGMENT -> AuthLoginFragment()
            AUTH_JOIN_FRAGMENT -> AuthJoinFragment()
            AUTH_FIND_PW_FRAGMENT -> AuthFindPwFragment()
            MAP_FRAGMENT -> MapFragment()
            MAP_SEARCH_REGION_FRAGMENT -> MapSearchRegionFragment()
            TRANSACTION_FRAGMENT -> TransactionFragment()
            MY_PAGE_FRAGMENT -> MyPageFragment()
            MY_PAGE_EDIT_NAME_FRAGMENT -> MyPageEditNameFragment()
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
            fragmentTransaction.replace(R.id.navHostFragmentMain, newFragment!!)

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

//    fun removeFragment2(name:String) {
//        navController.popBackStack()
//    }

    // 입력 요소에 포커스를 주는 메서드
    fun showSoftInput(view: View) {
        view.requestFocus()

        val inputMethodManger = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        thread {
            SystemClock.sleep(200)
            inputMethodManger.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    // Notification Channel을 등록하는 메서드
    // 첫 번째 : 코드에서 채널을 관리하기 위한 이름
    // 두 번째 : 사용자에게 노출 시킬 이름
    fun addNotificationChannel(id: String, name:String) {
        // 안드로이드 8.0 이상일 때만 동작하게 한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 알림 메시지를 관리하는 객체를 추출한다.
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // id를 통해 NotificationChannel 객체를 추출한다.
            // 채널이 등록된 적이 없다면 null을 반환한다.
            val channel = notificationManager.getNotificationChannel(id)
            // 채널이 등록된 적이 없다면...
            if (channel == null) {
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
    }

//    fun navigateTo(fragmentId: Int, bundle: Bundle? = null) {
//        navController.navigate(fragmentId, bundle, navOptions)
//    }

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


