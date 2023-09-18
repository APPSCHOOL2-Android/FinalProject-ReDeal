package com.hifi.redeal.map.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hifi.redeal.databinding.RowMapClientListBinding
import com.hifi.redeal.map.model.ClientDataClass

class MapBottomSheetRecyclerViewAdapter(private val data: MutableList<ClientDataClass>) :
        RecyclerView.Adapter<MapBottomSheetRecyclerViewAdapter.ResultViewHolder>() {
        inner class ResultViewHolder(rowMapClientListBinding: RowMapClientListBinding) :
            RecyclerView.ViewHolder(rowMapClientListBinding.root) {

            val rowMapClientListName: TextView
            val rowMapClientListManagerName: TextView
            val rowMapClientListDateRecent: TextView
            val rowMapClientListDateRecentLayout: LinearLayout
            val rowMapClientListTransactionType: ImageView
            val rowMapClientListBtnToNavi: Button
            val rowMapClientListBookMark : ImageView

            init {
                rowMapClientListName = rowMapClientListBinding.rowMapClientListName
                rowMapClientListManagerName = rowMapClientListBinding.rowMapClientListManagerName
                rowMapClientListDateRecent = rowMapClientListBinding.rowMapClientListDateRecent
                rowMapClientListTransactionType =
                    rowMapClientListBinding.rowMapClientListTransactionType
                rowMapClientListBtnToNavi = rowMapClientListBinding.rowMapClientListBtnToNavi
                rowMapClientListDateRecentLayout =
                    rowMapClientListBinding.rowMapClientListDateRecentLayout
                rowMapClientListBookMark =
                    rowMapClientListBinding.rowMapClientListBookMark
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
            val rowMapClientListBinding = RowMapClientListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
            val allViewHolder = ResultViewHolder(rowMapClientListBinding)

            rowMapClientListBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return allViewHolder
        }

        override fun getItemCount(): Int {
            return data.size!!
        }

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.rowMapClientListName.text =
                data.get(position)?.clientName
            holder.rowMapClientListManagerName.text =
                data.get(position)?.clientManagerName
            if (data.get(position)?.isBookmark==false){
                holder.rowMapClientListBookMark.visibility = View.INVISIBLE
            }
            holder.rowMapClientListDateRecentLayout.visibility = View.GONE
//            holder.rowMapClientListDateRecent.text = data.get(position)?
        }
    }
