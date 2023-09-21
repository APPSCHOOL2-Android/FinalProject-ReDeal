package com.hifi.redeal.transaction

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.DialogAddDepositBinding
import com.hifi.redeal.databinding.DialogAddTransactionBinding
import com.hifi.redeal.databinding.FragmentTransactionBinding
import com.hifi.redeal.databinding.RowTransactionBinding
import com.hifi.redeal.databinding.RowTransactionDepositBinding
import com.hifi.redeal.databinding.TransactionSelectClientBinding
import com.hifi.redeal.databinding.TransactionSelectClientItemBinding
import com.hifi.redeal.transaction.model.ClientSimpleData
import com.hifi.redeal.transaction.model.TransactionData
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionFragment : Fragment() {

    lateinit var fragmentTransactionBinding : FragmentTransactionBinding
    lateinit var mainActivity: MainActivity
    lateinit var transactionVM: TransactionViewModel
    var clientSimpleDataList = mutableListOf<ClientSimpleData>()

    lateinit var selBulider : AlertDialog.Builder
    lateinit var selDialog : AlertDialog

    lateinit var dialogAddDepositBinding: DialogAddDepositBinding
    lateinit var addDepositBuilder : AlertDialog.Builder
    lateinit var addDepositDialog : AlertDialog

    lateinit var dialogAddTransactionBinding: DialogAddTransactionBinding
    lateinit var addTransactionBuilder : AlertDialog.Builder
    lateinit var addTransactionDialog : AlertDialog

    var inoutMode = true

    var clientIdx : Long? = null
    var selectClientIdx: Long? = null
    val uid = Firebase.auth.uid!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentTransactionBinding = FragmentTransactionBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        clientIdx = arguments?.getLong("clientIdx")

        setViewModel()
        setClickEvent()

        return fragmentTransactionBinding.root

    }

    private fun setViewModel(){
        transactionVM = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        transactionVM.run{

            clientSimpleDataListVM.observe(viewLifecycleOwner){list ->
                clientSimpleDataList.clear()
                list.forEach {
                    clientSimpleDataList.add(it)
                }
            }

            transactionList.observe(viewLifecycleOwner){
                fragmentTransactionBinding.transactionListLayout.removeAllViews()

                var lastDate = ""
                var lastClientName = ""
                var totalSalesPrice = BigInteger("0") // 총 판매 금액
                var totalDepositPrice = BigInteger("0") // 총 받은 금액
                var salesCnt = 0

                val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

                it.sortByDescending { it.date }
                it.forEach { TransactionData ->
                    if(clientIdx != null){
                        if(clientIdx == TransactionData.clientIdx){
                            if(TransactionData.isDeposit){ // 입금 일 때

                                val transItem = RowTransactionDepositBinding.inflate(layoutInflater)

                                transItem.depositPriceTextView.text = "${formatAmount(TransactionData.transactionAmountReceived)} 원"
                                totalDepositPrice = totalDepositPrice.add(BigInteger(TransactionData.transactionAmountReceived))

                                val date = sdf.format(TransactionData.date.toDate())
                                if(date == lastDate){
                                    transItem.textTransactionDate.visibility = View.GONE
                                } else {
                                    transItem.textTransactionDate.text = date
                                    lastDate = date
                                }

                                if(TransactionData.clientName != null && lastClientName != TransactionData.clientName){
                                    transItem.transctionClientNameTextView.text = TransactionData.clientName
                                    lastClientName = TransactionData.clientName!!
                                } else {
                                    transItem.transctionClientNameTextView.visibility = View.GONE
                                }

                                fragmentTransactionBinding.transactionListLayout.addView(transItem.root)
                            } else { // 매출 일 때
                                salesCnt++
                                val transItem = RowTransactionBinding.inflate(layoutInflater)

                                val date = sdf.format(TransactionData.date.toDate())
                                if(date == lastDate){
                                    transItem.textTransactionDate.visibility = View.GONE
                                } else {
                                    transItem.textTransactionDate.text = date
                                    lastDate = date
                                }

                                if(TransactionData.clientName != null && lastClientName != TransactionData.clientName){
                                    transItem.transctionClientNameTextView.text = TransactionData.clientName
                                    lastClientName = TransactionData.clientName!!
                                } else {
                                    transItem.transctionClientNameTextView.visibility = View.GONE
                                }

                                transItem.textProductName.text = TransactionData.transactionName
                                transItem.textProductCount.text = formatAmount(TransactionData.transactionItemCount.toString())
                                transItem.textUnitPrice.text = formatAmount(TransactionData.transactionItemPrice)

                                val totalAmount = BigInteger(TransactionData.transactionItemPrice).multiply(
                                    BigInteger(TransactionData.transactionItemCount.toString())
                                )
                                transItem.textTotalAmount.text = formatAmount(totalAmount.toString())
                                transItem.textRecievedAmount.text = formatAmount(TransactionData.transactionAmountReceived)

                                val recievables = totalAmount.minus(BigInteger(TransactionData.transactionAmountReceived))
                                transItem.textRecievables.text = formatAmount(recievables.toString())

                                totalSalesPrice = totalSalesPrice.add(totalAmount) // 판매 금액
                                totalDepositPrice = totalDepositPrice.add(BigInteger(TransactionData.transactionAmountReceived)) // 받은 금액

                                fragmentTransactionBinding.transactionListLayout.addView(transItem.root)
                            }
                        }
                        fragmentTransactionBinding.textTotalSalesCount.text = "$salesCnt"
                        fragmentTransactionBinding.textTotalSales.text = formatAmount(totalSalesPrice.toString())
                        fragmentTransactionBinding.textTotalReceivables.text =
                            formatAmount(totalSalesPrice.minus(totalDepositPrice).toString())
                    } else {
                        if(TransactionData.isDeposit){ // 입금 일 때

                            val transItem = RowTransactionDepositBinding.inflate(layoutInflater)

                            transItem.depositPriceTextView.text = "${formatAmount(TransactionData.transactionAmountReceived)} 원"
                            totalDepositPrice = totalDepositPrice.add(BigInteger(TransactionData.transactionAmountReceived))

                            val date = sdf.format(TransactionData.date.toDate())
                            if(date == lastDate){
                                transItem.textTransactionDate.visibility = View.GONE
                            } else {
                                transItem.textTransactionDate.text = date
                                lastDate = date
                            }

                            if(TransactionData.clientName != null && lastClientName != TransactionData.clientName){
                                transItem.transctionClientNameTextView.text = TransactionData.clientName
                                lastClientName = TransactionData.clientName!!
                            } else {
                                transItem.transctionClientNameTextView.visibility = View.GONE
                            }

                            fragmentTransactionBinding.transactionListLayout.addView(transItem.root)
                        } else { // 매출 일 때
                            salesCnt++
                            val transItem = RowTransactionBinding.inflate(layoutInflater)

                            val date = sdf.format(TransactionData.date.toDate())
                            if(date == lastDate){
                                transItem.textTransactionDate.visibility = View.GONE
                            } else {
                                transItem.textTransactionDate.text = date
                                lastDate = date
                            }

                            if(TransactionData.clientName != null && lastClientName != TransactionData.clientName){
                                transItem.transctionClientNameTextView.text = TransactionData.clientName
                                lastClientName = TransactionData.clientName!!
                            } else {
                                transItem.transctionClientNameTextView.visibility = View.GONE
                            }

                            transItem.textProductName.text = TransactionData.transactionName
                            transItem.textProductCount.text = formatAmount(TransactionData.transactionItemCount.toString())
                            transItem.textUnitPrice.text = formatAmount(TransactionData.transactionItemPrice)

                            val totalAmount = BigInteger(TransactionData.transactionItemPrice).multiply(
                                BigInteger(TransactionData.transactionItemCount.toString())
                            )
                            transItem.textTotalAmount.text = formatAmount(totalAmount.toString())
                            transItem.textRecievedAmount.text = formatAmount(TransactionData.transactionAmountReceived)

                            val recievables = totalAmount.minus(BigInteger(TransactionData.transactionAmountReceived))
                            transItem.textRecievables.text = formatAmount(recievables.toString())

                            totalSalesPrice = totalSalesPrice.add(totalAmount) // 판매 금액
                            totalDepositPrice = totalDepositPrice.add(BigInteger(TransactionData.transactionAmountReceived)) // 받은 금액

                            fragmentTransactionBinding.transactionListLayout.addView(transItem.root)
                        }
                    }

                    fragmentTransactionBinding.textTotalSalesCount.text = "$salesCnt"
                    fragmentTransactionBinding.textTotalSales.text = formatAmount(totalSalesPrice.toString())
                    fragmentTransactionBinding.textTotalReceivables.text =
                        formatAmount(totalSalesPrice.minus(totalDepositPrice).toString())
                }
            }

            transactionVM.getAllTransactionData(uid)
            transactionVM.getNextTransactionIdx(uid)
            transactionVM.getUserAllClient(uid)
        }

    }

    private fun setClickEvent(){
        // 입금 버튼 클릭 이벤트 처리
        val addButtonLeft = fragmentTransactionBinding.ImgBtnAddDeposit
        addButtonLeft.setOnClickListener {
            inoutMode = true
            showDepositDialog()
        }

        // 거래 버튼 클릭 이벤트 처리
        val addButtonRight = fragmentTransactionBinding.ImgBtnAddTransaction
        addButtonRight.setOnClickListener {
            inoutMode = false
            showTransactionDialog()
        }
    }

        // 입금 추가 다이얼 로그 생성 함수
    private fun showDepositDialog() {

        dialogAddDepositBinding = DialogAddDepositBinding.inflate(layoutInflater)
        addDepositBuilder = AlertDialog.Builder(requireContext())
        addDepositDialog = addDepositBuilder.create()

        dialogAddDepositBinding.run{
            if(clientIdx != null){ // 거래처 화면에서 왔을 경우
                addSelectClientDepositBtn.visibility = View.GONE

                addDepositBtn.setOnClickListener {

                    if(addDepositPriceEditTextNumber.editableText.isNullOrEmpty()){
                        addDepositPriceinputLayout.error = "금액을 입력해 주세요."
                        addDepositPriceEditTextNumber.requestFocus()
                        return@setOnClickListener
                    }

                    val newTransactionData = TransactionData(
                        clientIdx!!,
                        Timestamp.now(),
                        true,
                        addDepositPriceEditTextNumber.editableText.toString(),
                        transactionVM.nextTransactionIdx,
                        0L,
                        "0",
                        ""
                    )
                    TransactionRepository.setTransactionData(uid,newTransactionData){
                        TransactionRepository.setClientTransactionDataList(uid,newTransactionData){
                            addDepositDialog.dismiss()
                            Snackbar.make(fragmentTransactionBinding.root, "입금 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                            transactionVM.getAllTransactionData(uid)
                            transactionVM.getNextTransactionIdx(uid)
                        }
                    }
                }
            } else { // 하단 네비게이션을 이용하여 왔을 경우
                addDepositBtn.setOnClickListener {
                    if(selectClientIdx == null){
                        addSelectClientDepositBtn.requestFocus()
                        addSelectClientDepositBtn.callOnClick()
                    } else {

                        if(addDepositPriceEditTextNumber.editableText.isNullOrEmpty()){
                            addDepositPriceinputLayout.error = "금액을 입력해 주세요."
                            addDepositPriceEditTextNumber.requestFocus()
                            return@setOnClickListener
                        }

                        val newTransactionData = TransactionData(
                            selectClientIdx!!,
                            Timestamp.now(),
                            true,
                            addDepositPriceEditTextNumber.editableText.toString(),
                            transactionVM.nextTransactionIdx,
                            0L,
                            "0",
                            ""
                        )
                        TransactionRepository.setTransactionData(uid,newTransactionData){
                            TransactionRepository.setClientTransactionDataList(uid,newTransactionData){
                                addDepositDialog.dismiss()
                                selectClientIdx = null
                                Snackbar.make(fragmentTransactionBinding.root, "입금 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                                transactionVM.getAllTransactionData(uid)
                                transactionVM.getNextTransactionIdx(uid)
                            }
                        }
                    }
                }
                addSelectClientDepositBtn.setOnClickListener {

                    val transactionSelectClientBinding = TransactionSelectClientBinding.inflate(layoutInflater)

                    clientSimpleDataList.clear()
                    transactionVM.clientSimpleDataListVM.value?.forEach {
                        clientSimpleDataList.add(it)
                    }

                    transactionSelectClientBinding.run{
                        searchClientResultRecyclerView.run{
                            adapter = SearchClientAdapter()
                            layoutManager = LinearLayoutManager(context)
                            addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
                        }

                        searchClientEditText.setOnEditorActionListener { v, actionId, event ->
                            clientSimpleDataList.clear()
                            transactionVM.clientSimpleDataListVM.value?.forEach {
                                if(it.clientName.contains(v.editableText) || it.clientManagerName.contains(v.editableText)){
                                    clientSimpleDataList.add(it)
                                }
                                searchClientResultRecyclerView.adapter?.notifyDataSetChanged()
                            }
                            true
                        }
                    }
                    selBulider = AlertDialog.Builder(requireContext())
                    selDialog = selBulider.create()
                    selDialog.setView(transactionSelectClientBinding.root)
                    selDialog.show()

                }
            }
        }
        addDepositDialog.setView(dialogAddDepositBinding.root)
        addDepositDialog.show()
    }

    private fun showTransactionDialog() { // 거래 추가 다이얼로그


        dialogAddTransactionBinding = DialogAddTransactionBinding.inflate(layoutInflater)
        addTransactionBuilder = AlertDialog.Builder(requireContext())
        addTransactionDialog = addTransactionBuilder.create()

        dialogAddTransactionBinding.run{
            if(clientIdx != null){
                selectTransactionClientBtn.visibility = View.GONE

                addTransactionBtn.setOnClickListener {

                    if(transactionNameEditText.editableText.isNullOrEmpty()){
                        transactionNameLayout.error = "품명을 입력해 주세요."
                        mainActivity.showSoftInput(transactionNameEditText)
                        return@setOnClickListener
                    }
                    if(transactionItemCountEditText.editableText.isNullOrEmpty()){
                        transactionItemCountLayout.error = "수량을 입력해 주세요."
                        mainActivity.showSoftInput(transactionItemCountEditText)
                        return@setOnClickListener
                    }
                    if(transactionItemPriceEditText.editableText.isNullOrEmpty()){
                        transactionItemPriceLayout.error = "단가를 입력해 주세요."
                        mainActivity.showSoftInput(transactionItemPriceEditText)
                        return@setOnClickListener
                    }
                    if(transactionAmountReceivedEditText.editableText.isNullOrEmpty()){
                        transactionAmountReceivedLayout.error = "받은 금액을 입력해 주세요."
                        mainActivity.showSoftInput(transactionAmountReceivedEditText)
                        return@setOnClickListener
                    }


                    val newTransactionData = TransactionData(
                        clientIdx!!,
                        Timestamp.now(),
                        false,
                        transactionAmountReceivedEditText.editableText.toString(),
                        transactionVM.nextTransactionIdx,
                        transactionItemCountEditText.editableText.toString().toLong(),
                        transactionItemPriceEditText.editableText.toString(),
                        transactionNameEditText.editableText.toString()
                    )
                    TransactionRepository.setTransactionData(uid,newTransactionData){
                        TransactionRepository.setClientTransactionDataList(uid, newTransactionData){
                            addTransactionDialog.dismiss()
                            Snackbar.make(fragmentTransactionBinding.root, "거래 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                            transactionVM.getAllTransactionData(uid)
                            transactionVM.getNextTransactionIdx(uid)
                        }
                    }
                }

            } else {
                addTransactionBtn.setOnClickListener {
                    if(selectClientIdx == null){
                        selectTransactionClientBtn.requestFocus()
                        selectTransactionClientBtn.callOnClick()
                    } else {

                        if(transactionNameEditText.editableText.isNullOrEmpty()){
                            transactionNameLayout.error = "품명을 입력해 주세요."
                            mainActivity.showSoftInput(transactionNameEditText)
                            return@setOnClickListener
                        }
                        if(transactionItemCountEditText.editableText.isNullOrEmpty()){
                            transactionItemCountLayout.error = "수량을 입력해 주세요."
                            mainActivity.showSoftInput(transactionItemCountEditText)
                            return@setOnClickListener
                        }
                        if(transactionItemPriceEditText.editableText.isNullOrEmpty()){
                            transactionItemPriceLayout.error = "단가를 입력해 주세요."
                            mainActivity.showSoftInput(transactionItemPriceEditText)
                            return@setOnClickListener
                        }
                        if(transactionAmountReceivedEditText.editableText.isNullOrEmpty()){
                            transactionAmountReceivedLayout.error = "받은 금액을 입력해 주세요."
                            mainActivity.showSoftInput(transactionAmountReceivedEditText)
                            return@setOnClickListener
                        }

                        val newTransactionData = TransactionData(
                            selectClientIdx!!,
                            Timestamp.now(),
                            false,
                            transactionAmountReceivedEditText.editableText.toString(),
                            transactionVM.nextTransactionIdx,
                            transactionItemCountEditText.text.toString().toLong(),
                            transactionItemPriceEditText.text.toString(),
                            transactionNameEditText.text.toString()
                        )
                        TransactionRepository.setTransactionData(uid,newTransactionData){
                            TransactionRepository.setClientTransactionDataList(uid,newTransactionData){
                                addTransactionDialog.dismiss()
                                selectClientIdx = null
                                Snackbar.make(fragmentTransactionBinding.root, "거래 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                                transactionVM.getAllTransactionData(uid)
                                transactionVM.getNextTransactionIdx(uid)
                            }
                        }
                    }
                }
                selectTransactionClientBtn.setOnClickListener {

                    val transactionSelectClientBinding = TransactionSelectClientBinding.inflate(layoutInflater)

                    clientSimpleDataList.clear()
                    transactionVM.clientSimpleDataListVM.value?.forEach {
                        clientSimpleDataList.add(it)
                    }

                    transactionSelectClientBinding.run{
                        searchClientResultRecyclerView.run{
                            adapter = SearchClientAdapter()
                            layoutManager = LinearLayoutManager(context)
                            addItemDecoration(MaterialDividerItemDecoration(context, MaterialDividerItemDecoration.VERTICAL))
                        }

                        searchClientEditText.setOnEditorActionListener { v, actionId, event ->
                            clientSimpleDataList.clear()
                            transactionVM.clientSimpleDataListVM.value?.forEach {
                                if(it.clientName.contains(v.editableText) || it.clientManagerName.contains(v.editableText)){
                                    clientSimpleDataList.add(it)
                                }
                                searchClientResultRecyclerView.adapter?.notifyDataSetChanged()
                            }
                            true
                        }
                    }
                    selBulider = AlertDialog.Builder(requireContext())
                    selDialog = selBulider.create()
                    selDialog.setView(transactionSelectClientBinding.root)
                    selDialog.show()

                }
            }
        }
        addTransactionDialog.setView(dialogAddTransactionBinding.root)
        addTransactionDialog.show()
    }

    // 금액을 000,000 형식으로 변환하는 함수
    private fun formatAmount(amount: String): String {
        val longAmount = amount.toBigIntegerOrNull() ?: BigInteger.ZERO
        return String.format("%,d", longAmount)
    }

    inner class SearchClientAdapter(): RecyclerView.Adapter<SearchClientAdapter.SearchClientViewHolder>(){
        inner class SearchClientViewHolder(transactionSelectClientItemBinding: TransactionSelectClientItemBinding):
            ViewHolder(transactionSelectClientItemBinding.root){

            val selectTransactionClientName = transactionSelectClientItemBinding.selectTransactionClientName
            val selectTransactionClinetState = transactionSelectClientItemBinding.selectTransactionClinetState
            val selectTransactionClientManagerName = transactionSelectClientItemBinding.selectTransactionClientManagerName
            val selectTransactionClientBookmarkView = transactionSelectClientItemBinding.selectTransactionClientBookmarkView

            init{
                transactionSelectClientItemBinding.root.setOnClickListener {
                    selectClientIdx = clientSimpleDataList[bindingAdapterPosition].clientIdx

                    if(inoutMode){
                        Snackbar.make(dialogAddDepositBinding.root,
                            "${clientSimpleDataList[bindingAdapterPosition].clientName} 선택 되었습니다.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(dialogAddTransactionBinding.root,
                            "${clientSimpleDataList[bindingAdapterPosition].clientName} 선택 되었습니다.", Snackbar.LENGTH_SHORT).show()
                    }

                    selDialog.dismiss()
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchClientViewHolder {
            val transactionSelectClientItemBinding = TransactionSelectClientItemBinding.inflate(layoutInflater)
            val viewHolder = SearchClientViewHolder(transactionSelectClientItemBinding)

            transactionSelectClientItemBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return viewHolder
        }
        override fun getItemCount(): Int {
            return clientSimpleDataList.size
        }

        override fun onBindViewHolder(holder: SearchClientViewHolder, position: Int) {
            when(clientSimpleDataList[position].clientState){
                1L -> {
                    holder.selectTransactionClinetState.setBackgroundResource(R.drawable.client_state_circle_trading)
                }
                2L -> {
                    holder.selectTransactionClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_try)
                }
                3L -> {
                    holder.selectTransactionClinetState.setBackgroundResource(R.drawable.client_state_circle_trade_stop)
                }
                else -> return
            }
            if(clientSimpleDataList[position].isBookmark){
                holder.selectTransactionClientBookmarkView.visibility = View.VISIBLE
                holder.selectTransactionClientBookmarkView.setBackgroundResource(R.drawable.star_fill_24px)
            } else {
                holder.selectTransactionClientBookmarkView.visibility = View.GONE
            }
            holder.selectTransactionClientName.text = clientSimpleDataList[position].clientName
            holder.selectTransactionClientManagerName.text = clientSimpleDataList[position].clientManagerName
        }

    }

}
