package com.hifi.redeal.map.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.RowMapRegionSearchBinding
import com.hifi.redeal.map.model.AdmVO
import com.hifi.redeal.map.vm.MapViewModel

class SearchRegionSiGunGuRecyclerViewAdapter(private val mainActivity: MainActivity, private val dataList: List<AdmVO>) :
    RecyclerView.Adapter<SearchRegionSiGunGuRecyclerViewAdapter.ViewHolder>() {
    val mapViewModel = ViewModelProvider(mainActivity)[MapViewModel::class.java]

    inner class ViewHolder(rowMapRegionSearchBinding: RowMapRegionSearchBinding) :
        RecyclerView.ViewHolder(rowMapRegionSearchBinding.root) {

        val rowMapRegionSearchName: TextView
        val rowMapRegionSearchLayout: LinearLayout

        init {
            rowMapRegionSearchName = rowMapRegionSearchBinding.rowMapRegionSearchName
            rowMapRegionSearchLayout = rowMapRegionSearchBinding.rowMapRegionSearchLayout

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(mainActivity)
        val rowMapRegionSearchBinding = RowMapRegionSearchBinding.inflate(layoutInflater)
        val allViewHolder = ViewHolder(rowMapRegionSearchBinding)

        rowMapRegionSearchBinding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        rowMapRegionSearchBinding.root.setOnClickListener {
            val position = allViewHolder.adapterPosition
            mapViewModel.run {
                getDongList(dataList.get(position).admCode)//전달
                currentDong.value = -1
                currentSiGunGu.value=position
                currentSiGunGu.observe(mainActivity){
                    notifyDataSetChanged()
                }
            }


        }

        return allViewHolder
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rowMapRegionSearchName.text= dataList.get(position).lowestAdmCodeNm

        if (position == mapViewModel.currentSiGunGu.value) {
            holder.rowMapRegionSearchLayout.setBackgroundColor(ContextCompat.getColor(mainActivity, R.color.primary95))
            holder.rowMapRegionSearchName.setTextColor(ContextCompat.getColor(mainActivity, R.color.primary20))
        } else {
            Log.d("포지션21",position.toString())
            holder.rowMapRegionSearchLayout.setBackgroundColor(Color.TRANSPARENT)
            holder.rowMapRegionSearchName.setTextColor(ContextCompat.getColor(mainActivity, R.color.text10))
        }
    }
}
