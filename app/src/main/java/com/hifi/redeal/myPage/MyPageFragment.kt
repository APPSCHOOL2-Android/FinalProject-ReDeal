package com.hifi.redeal.myPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.databinding.FragmentMyPageBinding
import com.hifi.redeal.myPage.repository.MyPageRepository

class MyPageFragment : Fragment() {

    private lateinit var fragmentMyPageBinding: FragmentMyPageBinding
    lateinit var mainActivity: MainActivity
    private val uid = Firebase.auth.uid
    private var userName = ""
    private var userEmail = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMyPageBinding = FragmentMyPageBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        val fragmentManager = mainActivity.supportFragmentManager
        fragmentMyPageBinding.run{
            userNameEditBtn.isEnabled = false
            MyPageRepository.getUserInfo(uid!!){documentSnapshot ->
                userName = documentSnapshot.get("userName") as String
                userEmail = documentSnapshot.get("userEmail") as String
                userNameTextView.text = userName
                userEmailTextView.text = userEmail
                userNameEditBtn.isEnabled = true
            }

            myPageToolbar.run{
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.MY_PAGE_FRAGMENT)
                }
            }
            userNameEditBtn.setOnClickListener {
                val newBundle = Bundle()
                newBundle.putString("uid", uid)
                newBundle.putString("userName", userName)
                mainActivity.replaceFragment(MainActivity.MY_PAGE_EDIT_NAME_FRAGMENT, true, newBundle)
            }
            userSignOutBtn.setOnClickListener {
                for (i in 0 until fragmentManager.backStackEntryCount) {
                    fragmentManager.popBackStack()
                }
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.remove("user_uid")
                editor.apply()
                Firebase.auth.signOut()
                mainActivity.replaceFragment(MainActivity.AUTH_LOGIN_FRAGMENT, false, null)
            }
            customerServiceBtn.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.MY_PAGE_REQUEST_FRAGMENT, true ,null)
            }
        }
        return fragmentMyPageBinding.root
    }
}