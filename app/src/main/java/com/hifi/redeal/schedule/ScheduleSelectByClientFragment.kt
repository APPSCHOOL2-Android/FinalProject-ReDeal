package com.hifi.redeal.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.FragmentScheduleSelectByClientBinding
import com.hifi.redeal.databinding.SelectScheduleClientItemBinding
import com.hifi.redeal.schedule.model.ClientSimpleData
import com.hifi.redeal.schedule.vm.ScheduleVM


class ScheduleSelectByClientFragment : Fragment() {

    lateinit var fragmentScheduleSelectByClientBinding: FragmentScheduleSelectByClientBinding
    lateinit var mainActivity: MainActivity
    lateinit var scheduleVM: ScheduleVM
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    var userClientSimpleDataList = mutableListOf<ClientSimpleData>()
    var userClientSimpleDataResultList = mutableListOf<ClientSimpleData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mainActivity = activity as MainActivity
        fragmentScheduleSelectByClientBinding = FragmentScheduleSelectByClientBinding.inflate(inflater)

        setViewModel()
        setBasicView()

        return fragmentScheduleSelectByClientBinding.root
    }

    private fun setViewModel(){
        scheduleVM = ViewModelProvider(requireActivity())[ScheduleVM::class.java]

        scheduleVM.run{
            userClientSimpleDataListVM.observe(viewLifecycleOwner){
                userClientSimpleDataList = it
                fragmentScheduleSelectByClientBinding.recyclerViewAllResult.adapter?.notifyDataSetChanged()
            }

            getUserAllClientInfo()
        }

    }

    private fun setBasicView(){

        fragmentScheduleSelectByClientBinding.run{
            recyclerViewAllResult.run{
                adapter = ClientListAdapter()
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
            }

            recyclerViewScheduleSearchListResult.run{
                adapter = ClientSearchResultAdapter()
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
            }

            searchViewClientList.run{
                hint = "찾고 있는 거래처를 입력해주세요"

                addTransitionListener { searchView, previousState, newState ->
                    // 서치바를 눌러 서치뷰가 보일 때
                    if(newState == SearchView.TransitionState.SHOWING){
                        userClientSimpleDataResultList.clear()
                    }

                }

                editText.setOnEditorActionListener { v, actionId, event ->
                    userClientSimpleDataList.forEach {

                        if(it.clientName.contains(v.editableText)
                            || it.clientManagerName.contains(v.editableText)
                        ) {
                            userClientSimpleDataResultList.add(it)
                            recyclerViewScheduleSearchListResult.adapter?.notifyDataSetChanged()
                        }
                    }
                    true
                }

            }
        }
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
                    if(scheduleVM.editScheduleData.value != null){
                        val temp = scheduleVM.editScheduleData.value
                        temp!!.clientIdx = userClientSimpleDataList[bindingAdapterPosition].clientIdx
                        scheduleVM.editScheduleData.postValue(temp)
                    }
                    scheduleVM.getUserSelectClientInfo(userClientSimpleDataList[bindingAdapterPosition].clientIdx)
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

    inner class ClientSearchResultAdapter(): RecyclerView.Adapter<ClientSearchResultAdapter.ClientViewHodel>(){
        inner class ClientViewHodel(selectScheduleClientItemBinding: SelectScheduleClientItemBinding): ViewHolder(selectScheduleClientItemBinding.root){
            val selectScheduleClientName = selectScheduleClientItemBinding.selectScheduleClientName
            val selectScheduleClinetState = selectScheduleClientItemBinding.selectScheduleClinetState
            val selectClientBtn = selectScheduleClientItemBinding.selectClientBtn
            val selectScheduleClientManagerName = selectScheduleClientItemBinding.selectScheduleClientManagerName
            val selectScheduleClientBookmarkView = selectScheduleClientItemBinding.selectScheduleClientBookmarkView
            init{
                selectClientBtn.setOnClickListener {
                    if(scheduleVM.editScheduleData.value != null){
                        val temp = scheduleVM.editScheduleData.value
                        temp!!.clientIdx = userClientSimpleDataResultList[bindingAdapterPosition].clientIdx
                        scheduleVM.editScheduleData.postValue(temp)
                    }
                    scheduleVM.getUserSelectClientInfo(userClientSimpleDataResultList[bindingAdapterPosition].clientIdx)
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
            return userClientSimpleDataResultList.size
        }

        override fun onBindViewHolder(holder: ClientViewHodel, position: Int) {
            when(userClientSimpleDataResultList[position].clientState){
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
            if(userClientSimpleDataResultList[position].isBookmark){
                holder.selectScheduleClientBookmarkView.setBackgroundResource(R.drawable.star_fill_24px)
            }
            holder.selectScheduleClientName.text = userClientSimpleDataResultList[position].clientName
            holder.selectScheduleClientManagerName.text = userClientSimpleDataResultList[position].clientManagerName

        }
    }

}