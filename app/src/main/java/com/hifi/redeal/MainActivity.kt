package com.hifi.redeal

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.transition.MaterialSharedAxis
import com.hifi.redeal.databinding.ActivityMainBinding
import com.hifi.redeal.memo.AddPhotoMemoFragment
import com.hifi.redeal.memo.AddRecordMemoFragment
import com.hifi.redeal.memo.PhotoDetailFragment
import com.hifi.redeal.memo.PhotoMemoFragment
import com.hifi.redeal.memo.RecordMemoFragment
import com.hifi.redeal.memo.SelectFragment

class MainActivity : AppCompatActivity() {

    lateinit var activityMainBinding: ActivityMainBinding
    var newFragment:Fragment? = null
    var oldFragment:Fragment? = null

    companion object{
        val PHOTO_MEMO_FRAGMENT = "PhotoMemoFragment"
        val RECORD_MEMO_FRAGMENT = "RecrodMemoFragment"
        val ADD_PHOTO_MEMO_FRAGMENT = "AddPhotoMemoFragment"
        val ADD_RECORD_MEMO_FRAGMENT = "AddRecordMemoFragment"
        val SELECT_FRAGMENT = "SelectFragment"
        val PHOTO_DETAIL_FRAGMENT = "PhotoDetailFragment"
    }

    private val permissionList = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_MEDIA_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val REQUEST_INTENTS = listOf(
        Intent.ACTION_GET_CONTENT,
        MediaStore.Audio.Media.RECORD_SOUND_ACTION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)

        requestPermissions(permissionList, 0)
        setContentView(activityMainBinding.root)
        replaceFragment(SELECT_FRAGMENT, false, null)
    }

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
            SELECT_FRAGMENT -> SelectFragment()
            PHOTO_MEMO_FRAGMENT -> PhotoMemoFragment()
            ADD_PHOTO_MEMO_FRAGMENT -> AddPhotoMemoFragment()
            PHOTO_DETAIL_FRAGMENT -> PhotoDetailFragment()
            RECORD_MEMO_FRAGMENT -> RecordMemoFragment()
            ADD_RECORD_MEMO_FRAGMENT -> AddRecordMemoFragment()
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
}

