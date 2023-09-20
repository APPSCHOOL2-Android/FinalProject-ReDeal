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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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
                tempClientSimpleDataList.clear()
                list.forEach {
                    tempClientSimpleDataList.add(it)
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
                                transItem.textProductCount.text = TransactionData.transactionItemCount.toString()
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
                            transItem.textProductCount.text = TransactionData.transactionItemCount.toString()
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

            transactionVM.getAllTransactionData()
            transactionVM.getNextTransactionIdx()
            transactionVM.getUserAllClient()
        }

    }

    private fun setClickEvent(){
        // 입금 버튼 클릭 이벤트 처리
        val addButtonLeft = fragmentTransactionBinding.ImgBtnAddDeposit
        addButtonLeft.setOnClickListener {
            showDepositDialog()
        }

        // 거래 버튼 클릭 이벤트 처리
        val addButtonRight = fragmentTransactionBinding.ImgBtnAddTransaction
        addButtonRight.setOnClickListener {
            showTransactionDialog()
        }
    }

        // 입금 추가 다이얼 로그 생성 함수
    private fun showDepositDialog() {
        val builder = AlertDialog.Builder(requireContext())

        val dialogAddDepositBinding = DialogAddDepositBinding.inflate(layoutInflater)

        val dialog = builder.create()

        dialogAddDepositBinding.run{
            if(clientIdx != null){ // 거래처 화면에서 왔을 경우
                addSelectClientDepositBtn.visibility = View.GONE

                addDepositBtn.setOnClickListener {
                    val newTransactionData = TransactionData(
                        clientIdx!!,
                        Timestamp.now(),
                        true,
                        editTextNumber.editableText.toString(),
                        transactionVM.nextTransactionIdx,
                        0L,
                        "0",
                        ""
                    )
                    TransactionRepository.setTransactionData(uid,newTransactionData){
                        TransactionRepository.setClientTransactionDataList(uid,newTransactionData){
                            dialog.dismiss()
                            Snackbar.make(fragmentTransactionBinding.root, "입금 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                            transactionVM.getAllTransactionData()
                            transactionVM.getNextTransactionIdx()
                        }
                    }
                }
            } else { // 하단 네비게이션을 이용하여 왔을 경우
                addDepositBtn.setOnClickListener {
                    if(selectClientIdx == null){
                        addSelectClientDepositBtn.requestFocus()
                        addSelectClientDepositBtn.callOnClick()
                    }
                }
                addSelectClientDepositBtn.setOnClickListener {
                    val selBulider = AlertDialog.Builder(requireContext())
                    val selDialog = selBulider.create()
                    val transactionSelectClientBinding = TransactionSelectClientBinding.inflate(layoutInflater)

                    transactionSelectClientBinding.run{
                        searchClientEditText.setOnEditorActionListener { v, actionId, event ->

                            true
                        }
                    }

                    //clientSimpleDataList
                }
            }
        }
        dialog.setView(dialogAddDepositBinding.root)
        dialog.show()
    }

    private fun showTransactionDialog() {
        val builder = AlertDialog.Builder(requireContext())

        val dialogAddTransactionBinding = DialogAddTransactionBinding.inflate(layoutInflater)

        val dialog = builder.create()

        dialogAddTransactionBinding.run{
            if(clientIdx != null){
                selectTransactionClientBtn.visibility = View.GONE

                addTransactionBtn.setOnClickListener {
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
                            dialog.dismiss()
                            Snackbar.make(fragmentTransactionBinding.root, "거래 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                            transactionVM.getAllTransactionData()
                            transactionVM.getNextTransactionIdx()
                        }
                    }
                }

            } else {

            }
        }
        dialog.setView(dialogAddTransactionBinding.root)
        dialog.show()
    }

    // 금액을 000,000 형식으로 변환하는 함수
    private fun formatAmount(amount: String): String {
        val longAmount = amount.toBigIntegerOrNull() ?: BigInteger.ZERO
        return String.format("%,d", longAmount)
    }

    inner class SearchClientAdapter(): RecyclerView.Adapter<SearchClientAdapter.SearchClientViewHolder>(){
        inner class SearchClientViewHolder(transactionSelectClientItemBinding: TransactionSelectClientItemBinding):
            ViewHolder(transactionSelectClientItemBinding.root){

            val selectScheduleClientName = transactionSelectClientItemBinding.selectTransactionClientName
            val selectScheduleClinetState = transactionSelectClientItemBinding.selectTransactionClinetState
            val selectClientBtn = transactionSelectClientItemBinding.selectClientBtn
            val selectScheduleClientManagerName = transactionSelectClientItemBinding.selectTransactionClientManagerName
            val selectScheduleClientBookmarkView = transactionSelectClientItemBinding.selectTransactionClientBookmarkView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchClientViewHolder {
            val transactionSelectClientItemBinding = TransactionSelectClientItemBinding.inflate(layoutInflater)
            val viewHolder = SearchClientViewHolder(transactionSelectClientItemBinding)

            return viewHolder
        }
        override fun getItemCount(): Int {
            return clientSimpleDataList.size
        }

        override fun onBindViewHolder(holder: SearchClientViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

    }

}
