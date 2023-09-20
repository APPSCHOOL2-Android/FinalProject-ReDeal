package com.hifi.redeal.transaction

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.hifi.redeal.MainActivity
import com.hifi.redeal.R
import com.hifi.redeal.databinding.DialogAddDepositBinding
import com.hifi.redeal.databinding.DialogAddTransactionBinding
import com.hifi.redeal.databinding.FragmentTransactionBinding
import com.hifi.redeal.databinding.RowTransactionBinding
import com.hifi.redeal.databinding.RowTransactionDepositBinding
import com.hifi.redeal.transaction.model.TransactionData
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionFragment : Fragment() {

    lateinit var fragmentTransactionBinding : FragmentTransactionBinding
    lateinit var mainActivity: MainActivity
    lateinit var transactionVM: TransactionViewModel
    var clientIdx : Long? = null
    var userIdx = "1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentTransactionBinding = FragmentTransactionBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        //clientIdx = arguments?.getLong("clientIdx")
        clientIdx = 1L

        setViewModel()
        setClickEvent()

        return fragmentTransactionBinding.root

    }

    private fun setViewModel(){
        transactionVM = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]

        transactionVM.run{
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
                    fragmentTransactionBinding.textTotalSalesCount.text = "$salesCnt"
                    fragmentTransactionBinding.textTotalSales.text = formatAmount(totalSalesPrice.toString())
                    fragmentTransactionBinding.textTotalReceivables.text =
                        formatAmount(totalSalesPrice.minus(totalDepositPrice).toString())
                }
            }

            transactionVM.getAllTransactionData(userIdx)
            transactionVM.getNextTransactionIdx(userIdx)
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
            if(clientIdx != null){
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
                    TransactionRepository.setTransactionData(userIdx,newTransactionData){
                        TransactionRepository.setClientTransactionDataList(userIdx, newTransactionData){
                            dialog.dismiss()
                            Snackbar.make(fragmentTransactionBinding.root, "입금 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                            transactionVM.getAllTransactionData(userIdx)
                            transactionVM.getNextTransactionIdx(userIdx)
                        }
                    }
                }
            } else {

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
                    TransactionRepository.setTransactionData(userIdx,newTransactionData){
                        TransactionRepository.setClientTransactionDataList(userIdx, newTransactionData){
                            dialog.dismiss()
                            Snackbar.make(fragmentTransactionBinding.root, "거래 내용 저장 완료 되었습니다.", Snackbar.LENGTH_SHORT).show()
                            transactionVM.getAllTransactionData(userIdx)
                            transactionVM.getNextTransactionIdx(userIdx)
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

//    inner class TransactionAdapter(private val items: MutableList<Any>) :
//        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//        // 뷰 홀더 유형 상수 정의
//        private val DEPOSIT_VIEW_TYPE = 1
//        private val TRANSACTION_VIEW_TYPE = 2
//
//        // ViewHolder 클래스 정의
//        inner class DepositViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            val textDepositDate: TextView = itemView.findViewById(R.id.textDepositDate)
//            val textDepositAmount: TextView = itemView.findViewById(R.id.textDepositAmount)
//        }
//
//        inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            val textTransactionDate: TextView = itemView.findViewById(R.id.textTransactionDate)
//            val textProductName: TextView = itemView.findViewById(R.id.textProductName)
//            val textProductCount: TextView = itemView.findViewById(R.id.textProductCount)
//            val textUnitPrice: TextView = itemView.findViewById(R.id.textUnitPrice)
//            val textAmountReceived: TextView = itemView.findViewById(R.id.textRecievedAmount)
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//            val inflater = LayoutInflater.from(parent.context)
//            return when (viewType) {
//                DEPOSIT_VIEW_TYPE -> {
//                    val depositView = inflater.inflate(R.layout.row_deposit, parent, false)
//                    DepositViewHolder(depositView)
//                }
//
//                TRANSACTION_VIEW_TYPE -> {
//                    val transactionView = inflater.inflate(R.layout.row_transaction, parent, false)
//                    TransactionViewHolder(transactionView)
//                }
//
//                else -> throw IllegalArgumentException("Invalid view type")
//            }
//        }
//
//        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//            val item = items[position]
//            when (holder) {
//                is DepositViewHolder -> {
//                    // 입금용 데이터 설정
//                    val depositItem = item as DepositItem
//                    holder.textDepositAmount.text = formatAmount(depositItem.depositAmount)
//                    holder.textDepositDate.text = depositItem.depositDate
//
//                    // 카드뷰를 클릭했을 때 수정 다이얼로그를 띄우도록 리스너 추가
//                    holder.itemView.setOnClickListener {
//                        showModifyDialog(holder)
//                    }
//                }
//
//                is TransactionViewHolder -> {
//                    // 거래용 데이터 설정
//                    val transactionItem = item as TransactionItem
//                    holder.textTransactionDate.text = transactionItem.transactionDate
//                    holder.textProductName.text = transactionItem.productName
//                    holder.textProductCount.text = transactionItem.productCount
//                    holder.textUnitPrice.text = formatAmount(transactionItem.unitPrice)
//                    holder.textAmountReceived.text = formatAmount(transactionItem.amountReceived)
//
//                    // 카드뷰를 클릭했을 때 수정 다이얼로그를 띄우도록 리스너 추가
//                    holder.itemView.setOnClickListener {
//                        showModifyDialog(holder)
//                    }
//                }
//            }
//        }
//
//        // 수정 다이얼로그 설정
//        private fun showModifyDialog(holder: RecyclerView.ViewHolder) {
//            val dialog = Dialog(requireContext())
//
//            when (holder) {
//                is DepositViewHolder -> {
//                    dialog.setContentView(R.layout.dialog_modify_deposit)
//                    // 입금 데이터를 수정하는 로직을 구현
//                }
//
//                is TransactionViewHolder -> {
//                    dialog.setContentView(R.layout.dialog_modify_transaction)
//                    // 거래 데이터를 수정하는 로직을 구현
//                }
//            }
//            dialog.show()
//        }
//
//
//
//        override fun getItemCount(): Int {
//            return items.size
//        }
//
//        override fun getItemViewType(position: Int): Int {
//            val item = items[position]
//            return when (item) {
//                is DepositItem -> DEPOSIT_VIEW_TYPE
//                is TransactionItem -> TRANSACTION_VIEW_TYPE
//                else -> throw IllegalArgumentException("Invalid item type")
//            }
//        }
//
//        // 입금용 데이터를 추가하는 메서드
//        fun addDeposit(deposit: DepositItem) {
//            items.add(deposit)
//            notifyDataSetChanged()
//        }
//
//        // 거래용 데이터를 추가하는 메서드
//        fun addTransaction(transaction: TransactionItem) {
//            items.add(transaction)
//            notifyDataSetChanged()
//        }
//    }
}

// 입금 데이터 클래스
data class DepositItem(
    val depositDate: String,
    val depositAmount: String
)

// 거래 데이터 클래스
data class TransactionItem(
    val transactionDate: String,
    val productName: String,
    val productCount: String,
    val unitPrice: String,
    val amountReceived: String
)