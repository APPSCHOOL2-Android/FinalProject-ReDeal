package com.hifi.redeal.account.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.account.repository.model.ClientData
import com.hifi.redeal.account.vm.AccountListViewModel
import com.hifi.redeal.databinding.RowFooterAccountListBinding
import com.hifi.redeal.databinding.RowItemAccountListBinding
import java.text.SimpleDateFormat

class AccountListAdapter(
    val mainActivity: MainActivity,
    val accountListViewModel: AccountListViewModel
): ListAdapter<ClientData, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ClientData>() {
            override fun areItemsTheSame(oldItem: ClientData, newItem: ClientData): Boolean {
                return oldItem.clientIdx == newItem.clientIdx
            }

            override fun areContentsTheSame(oldItem: ClientData, newItem: ClientData): Boolean {
                return oldItem == newItem
            }
        }
    }

    val ITEM = 0
    val FOOTER = 1

    val accountStateResIdList = arrayOf(
        R.drawable.circle_big_24px_primary20,
        R.drawable.circle_big_24px_primary50,
        R.drawable.circle_big_24px_primary80
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        when (viewType) {
            ITEM -> {
                val rowItemAccountListBinding = RowItemAccountListBinding.inflate(inflater)
                rowItemAccountListBinding.root.layoutParams = params
                return AccountListViewHolder(rowItemAccountListBinding)
            }
            else -> {
                val rowFooterAccountListBinding = RowFooterAccountListBinding.inflate(inflater)
                rowFooterAccountListBinding.root.layoutParams = params
                return AccountListFooterViewHolder(rowFooterAccountListBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AccountListViewHolder -> {
                holder.bind(currentList[position])
            }
            is AccountListFooterViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            FOOTER
        } else {
            ITEM
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    val drawable = mainActivity.getDrawable(R.drawable.star_fill_16px)

    inner class AccountListViewHolder(val rowItemAccountListBinding: RowItemAccountListBinding): RecyclerView.ViewHolder(rowItemAccountListBinding.root) {
        fun bind(clientData: ClientData) {
            rowItemAccountListBinding.run {
                root.setOnClickListener {
                    accountListViewModel.accountListRepository.incClientViewCount(mainActivity.uid, clientData.clientIdx ?: 0, clientData.viewCount ?: 0)

                    val bundle = Bundle()
                    bundle.putLong("clientIdx", clientData.clientIdx ?: 0)
                    mainActivity.replaceFragment(MainActivity.ACCOUNT_DETAIL_FRAGMENT, true, bundle)
//                    mainActivity.navigateTo(R.id.accountDetailFragment, bundle)
                }

                textViewRowItemAccountListAccountName.text = clientData.clientName
                if (clientData.isBookmark == true) {
                    textViewRowItemAccountListAccountName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.star_fill_16px, 0)
                } else {
                    textViewRowItemAccountListAccountName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
                imageViewRowItemAccountListTransactionState.setImageResource(accountStateResIdList[(clientData.clientState?.toInt() ?: 1) - 1] )
                textViewRowItemAccountListRepresentative.text = clientData.clientManagerName

                textViewRowItemAccountListRecentVisitDate.run {
                    if (clientData.recentVisitDate == null) {
                        visibility = View.INVISIBLE
                    } else {
                        visibility = View.VISIBLE

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val visitTime = dateFormat.format(clientData.recentVisitDate!!.toDate())

                        text = "최근 방문 ${mainActivity.intervalBetweenDateText(visitTime)}"
                    }
                }

                buttonRowItemAccountListCall.setOnClickListener {
                    val formattedPhoneNumber = PhoneNumberUtils.formatNumber(clientData.clientManagerPhone, "KR")

                    if (clientData.clientManagerPhone?.isNotEmpty() == true) {
                        AlertDialog.Builder(mainActivity)
                            .setTitle("담당자 연락처 통화")
                            .setMessage("${formattedPhoneNumber}\n이 번호로 통화하시겠습니까?")
                            .setPositiveButton("확인") { _, _ ->
                                mainActivity.startActivity(
                                    Intent(
                                        "android.intent.action.CALL",
                                        Uri.parse("tel:${clientData.clientManagerPhone}")
                                    )
                                )
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    } else {
                        Snackbar.make(mainActivity.activityMainBinding.root, "등록된 담당자 연락처가 없습니다", Snackbar.LENGTH_SHORT).apply {
                            anchorView = mainActivity.activityMainBinding.bottomNavigationViewMain
                        }.show()
                    }
                }
            }
        }
    }

    inner class AccountListFooterViewHolder(val rowFooterAccountListBinding: RowFooterAccountListBinding): RecyclerView.ViewHolder(rowFooterAccountListBinding.root) {
        fun bind() {
            if (currentList.isEmpty()) {
                rowFooterAccountListBinding.textViewRowFooterAccountList.text = "거래처가 없습니다"
            } else {
                val criterion = when (accountListViewModel.selectedTabItemPosState.value) {
                    0 -> "즐겨 찾기"
                    1 -> "거래 중"
                    2 -> "거래 시도"
                    3 -> "거래 중지"
                    else -> "총"
                }

                rowFooterAccountListBinding.textViewRowFooterAccountList.text = "$criterion ${currentList.size}개"
            }
        }
    }
}