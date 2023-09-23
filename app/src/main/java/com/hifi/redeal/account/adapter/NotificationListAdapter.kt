package com.hifi.redeal.account.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hifi.redeal.MainActivity
import com.hifi.redeal.account.repository.NotificationRepository
import com.hifi.redeal.account.repository.model.NotificationData
import com.hifi.redeal.databinding.RowItemNotificationBinding
import java.text.SimpleDateFormat

class NotificationListAdapter(
    val mainActivity: MainActivity,
    val notificationRepository: NotificationRepository
): ListAdapter<NotificationData, NotificationListAdapter.NotificationViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<NotificationData>() {
            override fun areItemsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val rowItemNotificationBinding = RowItemNotificationBinding.inflate(inflater)

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rowItemNotificationBinding.root.layoutParams = params

        return NotificationViewHolder(rowItemNotificationBinding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class NotificationViewHolder(
        val rowItemNotificationBinding: RowItemNotificationBinding
    ): RecyclerView.ViewHolder(rowItemNotificationBinding.root) {

        fun bind(notification: NotificationData) {

            rowItemNotificationBinding.run {
                root.setOnClickListener {
                    notificationRepository.setChecked(mainActivity.uid, notification.notificationId ?: "")

                    val bundle = Bundle()
                    bundle.putLong("clientIdx", notification.clientIdx ?: 0)
                    bundle.putString("senderId", notification.senderId)
                    bundle.putBoolean("fromNotification", true)
                    mainActivity.replaceFragment(MainActivity.ACCOUNT_EDIT_FRAGMENT, true, bundle)
                }

                notificationRepository.getNotificationSender(notification.senderId ?: "") {
                    textViewRowItemNotificationSender.text = "${it?.userName}님이 공유한 거래처입니다"

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val visitTime = dateFormat.format(notification.notifyTime!!.toDate())

                    textViewRowItemNotificationNotifyDate.text = mainActivity.intervalBetweenDateText(visitTime)
                }

                notificationRepository.getNotificationClient(notification.senderId ?: "", notification.clientIdx ?: 0) { client ->
                    textViewRowItemNotificationAccountName.text = client?.clientName
                    textViewRowItemNotificationRepresentative.text = client?.clientManagerName
                }
            }
        }
    }
}