package com.hifi.redeal.account

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.account.adapter.AccountListAdapter
import com.hifi.redeal.account.vm.AccountListViewModel
import com.hifi.redeal.databinding.FragmentAccountListBinding
import com.hifi.redeal.databinding.RowFooterAccountListBinding
import com.hifi.redeal.databinding.RowItemAccountListBinding
import com.hifi.redeal.databinding.TabItemLayoutAccountListStateBinding

class AccountListFragment : Fragment() {

    lateinit var fragmentAccountListBinding: FragmentAccountListBinding
    lateinit var mainActivity: MainActivity

    lateinit var accountListViewModel: AccountListViewModel

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

    val tabItemListState = mutableListOf<Tab>()

    val tabItemChipListSort = mutableListOf<Chip>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentAccountListBinding = FragmentAccountListBinding.inflate(layoutInflater)

        accountListViewModel = ViewModelProvider(this)[AccountListViewModel::class.java]

        fragmentAccountListBinding.run {

            val accountListAdapter = AccountListAdapter(mainActivity, accountListViewModel)

            tabLayoutAccountListState.run {

                val indicatorColor = ContextCompat.getColor(context, R.color.primary20)

                tabItemListState.clear()

                for (i in 0..3) {
                    val tab = newTab()
                    val tabItemBinding = TabItemLayoutAccountListStateBinding.inflate(layoutInflater)
                    tabItemBinding.imageViewTabItemAccountListCriterion.setImageResource(tabItemStateInfoList[0][i] as Int)
                    tabItemBinding.textViewTabItemAccountListCriterion.text = tabItemStateInfoList[1][i] as String
                    tabItemBinding.textViewTabItemAccountListCount.text = (tabItemStateInfoList[2][i] as Int).toString()
                    tab.setCustomView(tabItemBinding.root)
                    tabItemListState.add(tab)
                    addTab(tab)
                }

                addOnTabSelectedListener(object : OnTabSelectedListener{
                    override fun onTabSelected(tab: Tab?) {
                        accountListViewModel.setSelectedTabItemPosState(tab?.position ?: -1)
                    }

                    override fun onTabUnselected(tab: Tab?) {
                    }

                    override fun onTabReselected(tab: Tab?) {
                        accountListViewModel.setSelectedTabItemPosState(-1)
                    }
                })

                selectTab(null)

                var tabStateInit = true

                accountListViewModel.selectedTabItemPosState.observe(viewLifecycleOwner) { position ->
                    if (position == -1) {
                        tabStateInit = false
                        selectTab(null)
                        setSelectedTabIndicatorColor(Color.TRANSPARENT)
                    } else {
                        if (tabStateInit) {
                            tabStateInit = false
                            selectTab(tabItemListState[position])
                            return@observe
                        }
                        setSelectedTabIndicatorColor(indicatorColor)
                    }
                    accountListViewModel.getClientList(1)
                }
            }

            tabItemChipListSort.clear()

            tabItemChipListSort.add(chipAccountListSortReference)
            tabItemChipListSort.add(chipAccountListSortVisit)
            tabItemChipListSort.add(chipAccountListSortContact)
            tabItemChipListSort.add(chipAccountListSortRegister)

            accountListViewModel.run {
                selectTabSort(tabItemCheckedListSort.indexOf(true))
                if (!tabItemDescListSort[tabItemCheckedListSort.indexOf(true)]) {
                    tabItemChipListSort[tabItemCheckedListSort.indexOf(true)].setCloseIconResource(R.drawable.arrow_drop_up_24px)
                }

                chipAccountListSortReference.run {
                    setOnClickListener {
                        if (tabItemCheckedListSort[0]) {
                            changeAscDesc(0)
                        } else {
                            selectTabSort(0)
                        }
                        accountListViewModel.getClientList(1)
                    }
                }

                chipAccountListSortVisit.run {
                    setOnClickListener {
                        if (tabItemCheckedListSort[1]) {
                            changeAscDesc(1)
                        } else {
                            selectTabSort(1)
                        }
                        accountListViewModel.getClientList(1)
                    }
                }

                chipAccountListSortContact.run {
                    setOnClickListener {
                        if (tabItemCheckedListSort[2]) {
                            changeAscDesc(2)
                        } else {
                            selectTabSort(2)
                        }
                        accountListViewModel.getClientList(1)
                    }
                }

                chipAccountListSortRegister.run {
                    setOnClickListener {
                        if (tabItemCheckedListSort[3]) {
                            changeAscDesc(3)
                        } else {
                            selectTabSort(3)
                        }
                        accountListViewModel.getClientList(1)
                    }
                }
            }

            recyclerViewAccountList.run {
                adapter = accountListAdapter
                layoutManager = LinearLayoutManager(mainActivity)
                addItemDecoration(DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL))
            }

            accountListViewModel.clientList.observe(viewLifecycleOwner) {
                accountListAdapter.run {
                    submitList(it) {
                        notifyItemChanged(itemCount - 1)
                    }
                }
            }

            fabAccountListAddAccount.setOnClickListener {
                mainActivity.navigateTo(R.id.accountEditFragment)
            }
        }

        return fragmentAccountListBinding.root
    }

    fun changeAscDesc(tabIdx: Int) {
        if (accountListViewModel.tabItemDescListSort[tabIdx]) {
            tabItemChipListSort[tabIdx].setCloseIconResource(R.drawable.arrow_drop_up_24px)
        } else {
            tabItemChipListSort[tabIdx].setCloseIconResource(R.drawable.arrow_drop_down_24px)
        }
        accountListViewModel.tabItemDescListSort[tabIdx] = !accountListViewModel.tabItemDescListSort[tabIdx]
    }

    fun selectTabSort(tabIdx: Int) {
        for (i in 0..3) {
            if (i == tabIdx) {
                accountListViewModel.tabItemCheckedListSort[i] = true
                tabItemChipListSort[i].run {
                    isCloseIconVisible = true
                }
            } else {
                accountListViewModel.tabItemCheckedListSort[i] = false
                tabItemChipListSort[i].run {
                    isCloseIconVisible = false
                }
            }
        }
    }
}