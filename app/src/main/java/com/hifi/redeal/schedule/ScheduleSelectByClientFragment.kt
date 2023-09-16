package com.hifi.redeal.schedule

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentMakeScheduleBinding
import com.hifi.redeal.databinding.FragmentScheduleSelectByClientBinding
import com.hifi.redeal.databinding.SelectScheduleClientItemBinding
import com.hifi.redeal.schedule.model.ClientSimpleData
import com.hifi.redeal.schedule.vm.ScheduleVM


class ScheduleSelectByClientFragment : Fragment() {

    lateinit var fragmentScheduleSelectByClientBinding: FragmentScheduleSelectByClientBinding
    lateinit var mainActivity: MainActivity
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    var userIdx = "1"
    var userClientSimpleDataList = mutableListOf<ClientSimpleData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mainActivity = activity as MainActivity
        fragmentScheduleSelectByClientBinding = FragmentScheduleSelectByClientBinding.inflate(inflater)

        scheduleVM = ViewModelProvider(mainActivity)[ScheduleVM::class.java]
        scheduleVM.run{
            userClientSimpleDataListVM.observe(mainActivity){
                userClientSimpleDataList = it
                fragmentScheduleSelectByClientBinding.recyclerViewAllResult.adapter?.notifyDataSetChanged()
            }
            getUserAllClientInfo(userIdx)
        }

        fragmentScheduleSelectByClientBinding.run{
            recyclerViewAllResult.run{
                adapter = ClientListAdapter()
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
            }
        }

        return fragmentScheduleSelectByClientBinding.root
    }

    override fun onStart() {
        super.onStart()
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainActivity.removeFragment(MainActivity.SCHEDULE_SELECT_BY_CLIENT_FRAGMENT)
                onBackPressedCallback.remove()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    inner class ClientListAdapter(): RecyclerView.Adapter<ClientListAdapter.ClientViewHodel>(){
        inner class ClientViewHodel(selectScheduleClientItemBinding: SelectScheduleClientItemBinding): ViewHolder(selectScheduleClientItemBinding.root){
            val selectScheduleClientName = selectScheduleClientItemBinding.selectScheduleClientName
            val selectScheduleClinetState = selectScheduleClientItemBinding.selectScheduleClinetState
            val selectClientBtn = selectScheduleClientItemBinding.selectClientBtn
            val selectScheduleClientManagerName = selectScheduleClientItemBinding.selectScheduleClientManagerName
            val selectScheduleClientBookmarkView = selectScheduleClientItemBinding.selectScheduleClientBookmarkView
            init{
                selectClientBtn.setOnClickListener {
                    scheduleVM = ViewModelProvider(mainActivity)[ScheduleVM::class.java]
                    scheduleVM.getUserSelectClientInfo(userIdx, userClientSimpleDataList[bindingAdapterPosition].clientIdx.toString())
                    mainActivity.removeFragment(MainActivity.SCHEDULE_SELECT_BY_CLIENT_FRAGMENT)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHodel {
            val selectScheduleClientItemBinding = SelectScheduleClientItemBinding.inflate(layoutInflater)
            val clientViewHodel = ClientViewHodel(selectScheduleClientItemBinding)

            selectScheduleClientItemBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return clientViewHodel

        }

        override fun getItemCount(): Int {
            return userClientSimpleDataList.size
        }

        override fun onBindViewHolder(holder: ClientViewHodel, position: Int) {
            when(userClientSimpleDataList[position].clientState){
                1L -> {
                    holder.selectScheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trading)
                }
                2L -> {
                    holder.selectScheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
                }
                3L -> {
                    holder.selectScheduleClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
                }
                else -> return
            }
            if(userClientSimpleDataList[position].isBookmark){
                holder.selectScheduleClientBookmarkView.setBackgroundResource(R.drawable.star_fill_24px)
            }
            holder.selectScheduleClientName.text = userClientSimpleDataList[position].clientName
            holder.selectScheduleClientManagerName.text = userClientSimpleDataList[position].clientManagerName

        }
    }

}