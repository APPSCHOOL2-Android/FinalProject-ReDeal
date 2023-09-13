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
}