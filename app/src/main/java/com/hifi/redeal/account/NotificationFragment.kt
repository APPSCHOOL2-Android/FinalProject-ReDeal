package com.hifi.redeal.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.account.adapter.NotificationListAdapter
import com.hifi.redeal.account.repository.NotificationRepository
import com.hifi.redeal.databinding.FragmentNotificationBinding

class NotificationFragment : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var fragmentNotificationBinding: FragmentNotificationBinding

    val notificationRepository = NotificationRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentNotificationBinding = FragmentNotificationBinding.inflate(layoutInflater)

        fragmentNotificationBinding.run {
            materialToolbarNotification.run {
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.NOTIFICATION_FRAGMENT)
                }
            }

            val notificationListAdapter = NotificationListAdapter(mainActivity, notificationRepository)

            recyclerViewNotification.run {
                adapter = notificationListAdapter
                layoutManager = LinearLayoutManager(mainActivity)
                addItemDecoration(DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL))
            }

            notificationRepository.getNotificationList(mainActivity.uid) {
                notificationListAdapter.submitList(it)
            }
        }

        return fragmentNotificationBinding.root
    }
}