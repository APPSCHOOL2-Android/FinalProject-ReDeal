package com.hifi.redeal.account

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentAccountListBinding
import com.hifi.redeal.databinding.RowFooterAccountListBinding
import com.hifi.redeal.databinding.RowItemAccountListBinding
import com.hifi.redeal.databinding.TabItemLayoutAccountListSortBinding
import com.hifi.redeal.databinding.TabItemLayoutAccountListStateBinding

class AccountListFragment : Fragment() {

    lateinit var fragmentAccountListBinding: FragmentAccountListBinding
    lateinit var mainActivity: MainActivity

    val tabItemStateInfoList = arrayOf(
        arrayOf(
            R.drawable.star_fill_24px,
            R.drawable.circle_24px_primary20,
            R.drawable.circle_24px_primary50,
            R.drawable.circle_24px_primary80
        ),
        arrayOf(
            "즐겨 찾기", "거래 중", "거래 시도", "거래 중지"
        ),
        arrayOf(
            12, 48, 10000, 92
        )
    )

    val tabItemSortBindingList = mutableListOf<TabItemLayoutAccountListSortBinding>()

    val tabItemSortDescList = mutableListOf(true, true, true, true)

    var firstSelect = true

    var criterion = "총"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentAccountListBinding = FragmentAccountListBinding.inflate(layoutInflater)

        fragmentAccountListBinding.run {
            tabLayoutAccountListState.run {
                setSelectedTabIndicatorColor(Color.TRANSPARENT)

                val indicatorColor = ContextCompat.getColor(context, R.color.primary20)

                addOnTabSelectedListener(object : OnTabSelectedListener{
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        if (!firstSelect) {
                            setSelectedTabIndicatorColor(indicatorColor)
                            criterion = when (tab?.position) {
                                0 -> "즐겨 찾기"
                                1 -> "거래 중"
                                2 -> "거래 시도"
                                3 -> "거래 중지"
                                else -> "총"
                            }
                            recyclerViewAccountList.adapter?.notifyDataSetChanged()
                        }
                        firstSelect = false
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                        setSelectedTabIndicatorColor(indicatorColor)
                        criterion = when (tab?.position) {
                            0 -> "즐겨 찾기"
                            1 -> "거래 중"
                            2 -> "거래 시도"
                            3 -> "거래 중지"
                            else -> "총"
                        }
                        recyclerViewAccountList.adapter?.notifyDataSetChanged()
                    }
                })

                for (i in 0..3) {
                    val tab = newTab()
                    val tabItemBinding = TabItemLayoutAccountListStateBinding.inflate(layoutInflater)
                    tabItemBinding.imageViewTabItemAccountListCriterion.setImageResource(tabItemStateInfoList[0][i] as Int)
                    tabItemBinding.textViewTabItemAccountListCriterion.text = tabItemStateInfoList[1][i] as String
                    tabItemBinding.textViewTabItemAccountListCount.text = (tabItemStateInfoList[2][i] as Int).toString()
                    tab.setCustomView(tabItemBinding.root)
                    addTab(tab)
                }
            }

            tabItemSortBindingList.add(TabItemLayoutAccountListSortBinding.bind(layoutAccountListSortReference.root))
            tabItemSortBindingList.add(TabItemLayoutAccountListSortBinding.bind(layoutAccountListSortVisit.root))
            tabItemSortBindingList.add(TabItemLayoutAccountListSortBinding.bind(layoutAccountListSortContact.root))
            tabItemSortBindingList.add(TabItemLayoutAccountListSortBinding.bind(layoutAccountListSortRegister.root))

            layoutAccountListSortReference.run {
                root.isSelected = true
                imageViewTabItemAccountListSorting.isVisible = true
                textViewTabItemAccountListSorting.text = "조회순"

                root.setOnClickListener {
                    if (root.isSelected) {
                        changeAscDesc(0)
                    } else {
                        selectTab(0)
                    }
                }
            }

            layoutAccountListSortVisit.run {
                textViewTabItemAccountListSorting.text = "방문순"

                root.setOnClickListener {
                    if (root.isSelected) {
                        changeAscDesc(1)
                    } else {
                        selectTab(1)
                    }
                }
            }

            layoutAccountListSortContact.run {
                textViewTabItemAccountListSorting.text = "연락순"

                root.setOnClickListener {
                    if (root.isSelected) {
                        changeAscDesc(2)
                    } else {
                        selectTab(2)
                    }
                }
            }

            layoutAccountListSortRegister.run {
                textViewTabItemAccountListSorting.text = "등록순"

                root.setOnClickListener {
                    if (root.isSelected) {
                        changeAscDesc(3)
                    } else {
                        selectTab(3)
                    }
                }
            }

            recyclerViewAccountList.run {
                adapter = AccountListAdapter(20)
                layoutManager = LinearLayoutManager(activity)
                addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            }
        }

        return fragmentAccountListBinding.root
    }

    fun changeAscDesc(tabIdx: Int) {
        if (tabItemSortDescList[tabIdx]) {
            tabItemSortBindingList[tabIdx].imageViewTabItemAccountListSorting.setImageResource(R.drawable.arrow_drop_up_24px)
        } else {
            tabItemSortBindingList[tabIdx].imageViewTabItemAccountListSorting.setImageResource(R.drawable.arrow_drop_down_24px)
        }
        tabItemSortDescList[tabIdx] = !tabItemSortDescList[tabIdx]
    }

    fun selectTab(tabIdx: Int) {
        for (i in 0..3) {
            if (i == tabIdx) {
                tabItemSortBindingList[i].run {
                    root.isSelected = true
                    imageViewTabItemAccountListSorting.isVisible = true
                }
            } else {
                tabItemSortBindingList[i].run {
                    root.isSelected = false
                    imageViewTabItemAccountListSorting.isVisible = false
                }
            }
        }
    }

    inner class AccountListAdapter(val size: Int): RecyclerView.Adapter<ViewHolder>() {

        val ITEM = 0
        val FOOTER = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val params = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            when (viewType) {
                ITEM -> {
                    val rowItemAccountListBinding = RowItemAccountListBinding.inflate(layoutInflater)
                    rowItemAccountListBinding.root.layoutParams = params
                    return AccountListViewHolder(rowItemAccountListBinding)
                }
                else -> {
                    val rowFooterAccountListBinding = RowFooterAccountListBinding.inflate(layoutInflater)
                    rowFooterAccountListBinding.root.layoutParams = params
                    return AccountListFooterViewHolder(rowFooterAccountListBinding)
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (holder) {
                is AccountListViewHolder -> {
                    holder.bind(position)
                }
                is AccountListFooterViewHolder -> {
                    holder.bind()
                }
            }
        }

        override fun getItemCount(): Int {
            return size + 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == itemCount - 1) {
                FOOTER
            } else {
                ITEM
            }
        }

        inner class AccountListViewHolder(val rowItemAccountListBinding: RowItemAccountListBinding): RecyclerView.ViewHolder(rowItemAccountListBinding.root) {
            fun bind(position: Int) {
                var accountName = ""
                val cnt = position % 3 + 1
                for (i in 1..cnt) {
                    accountName += "ABC Company"
                }
                rowItemAccountListBinding.textViewRowItemAccountListAccountName.text = accountName
                if (position in 3..5) {
                    rowItemAccountListBinding.textViewRowItemAccountListAccountName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.star_fill_20px, 0)
                }
                var representative = ""
                val cnt2 = position % 5 + 1
                for (i in 1..cnt2) {
                    representative += "이소은$position"
                }
                rowItemAccountListBinding.textViewRowItemAccountListRepresentative.text = representative

                when (position % 3) {
                    0 -> {
                        rowItemAccountListBinding.imageViewRowItemAccountListTransactionState.setImageResource(R.drawable.circle_big_24px_primary20)
                    }
                    1 -> {
                        rowItemAccountListBinding.imageViewRowItemAccountListTransactionState.setImageResource(R.drawable.circle_big_24px_primary50)
                    }
                    2 -> {
                        rowItemAccountListBinding.imageViewRowItemAccountListTransactionState.setImageResource(R.drawable.circle_big_24px_primary80)
                    }
                }
            }
        }

        inner class AccountListFooterViewHolder(val rowFooterAccountListBinding: RowFooterAccountListBinding): RecyclerView.ViewHolder(rowFooterAccountListBinding.root) {
            fun bind() {
                if (size == 0) {
                    rowFooterAccountListBinding.textViewRowFooterAccountList.text = "거래처가 없습니다"
                } else {
                    rowFooterAccountListBinding.textViewRowFooterAccountList.text = "$criterion ${itemCount - 1}개"
                }
            }
        }
    }
}