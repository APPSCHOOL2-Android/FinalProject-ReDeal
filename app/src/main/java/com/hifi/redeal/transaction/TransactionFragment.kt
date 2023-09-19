package com.hifi.redeal.transaction

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hifi.redeal.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_transaction, container, false)

        // 왼쪽 버튼 클릭 이벤트 처리
        val addButtonLeft = rootView.findViewById<ImageButton>(R.id.ImgBtnAddDeposit)
        addButtonLeft.setOnClickListener {
            showDepositDialog()
        }

        // 오른쪽 버튼 클릭 이벤트 처리
        val addButtonRight = rootView.findViewById<ImageButton>(R.id.ImgBtnAddTransaction)
        addButtonRight.setOnClickListener {
            showTransactionDialog()
        }


        // RecyclerView 초기화
        recyclerView = rootView.findViewById(R.id.recyclerTransaction)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화
        adapter = TransactionAdapter(mutableListOf())
        recyclerView.adapter = adapter

        return rootView
    }

    private fun showDepositDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_deposit, null)

        builder.setView(dialogView)

        val editTextDeposit = dialogView.findViewById<EditText>(R.id.textInputEditTextDeposit)
        val addDepositButton = dialogView.findViewById<Button>(R.id.buttonAddDeposit)

        val dialog = builder.create()
        dialog.show()

        // '추가' 버튼 클릭 이벤트 처리
        addDepositButton.setOnClickListener {
            val deposit = editTextDeposit.text.toString()

            // 사용자 입력을 처리하고 원하는 데이터를 추출하여 카드뷰 생성 로직 추가
            if (deposit.isNotEmpty()) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val depositItem = DepositItem(currentDate, deposit)
                // 어댑터에 데이터 추가
                adapter.addDeposit(depositItem)
                dialog.dismiss()
            }
        }
    }

    private fun showTransactionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_transaction, null)

        builder.setView(dialogView)

        val editTextProductName =
            dialogView.findViewById<EditText>(R.id.textInputEditTextProductName)
        val editTextProductCount =
            dialogView.findViewById<EditText>(R.id.textInputEditTextProductCount)
        val editTextUnitPrice = dialogView.findViewById<EditText>(R.id.textInputEditTextUnitPrice)
        val editTextAmountReceived =
            dialogView.findViewById<EditText>(R.id.textInputEditTextAmountReceived)
        val addTransactionButton = dialogView.findViewById<Button>(R.id.buttonAddTransaction)

        val dialog = builder.create()
        dialog.show()

        // '추가' 버튼 클릭 이벤트 처리
        addTransactionButton.setOnClickListener {
            val productName = editTextProductName.text.toString()
            val productCount = editTextProductCount.text.toString()
            val unitPrice = editTextUnitPrice.text.toString()
            val amountReceived = editTextAmountReceived.text.toString()

            // 사용자 입력을 처리하고 원하는 데이터를 추출하여 카드뷰 생성 로직 추가
            if (productName.isNotEmpty() && productCount.isNotEmpty() && unitPrice.isNotEmpty() && amountReceived.isNotEmpty()) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val transactionItem = TransactionItem(
                    currentDate,
                    productName,
                    productCount,
                    unitPrice,
                    amountReceived
                )
                // 어댑터에 데이터 추가
                adapter.addTransaction(transactionItem)
                dialog.dismiss()
            }
        }
    }

    inner class TransactionAdapter(private val items: MutableList<Any>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        // 뷰 홀더 유형 상수 정의
        private val DEPOSIT_VIEW_TYPE = 1
        private val TRANSACTION_VIEW_TYPE = 2

        // ViewHolder 클래스 정의
        inner class DepositViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textDepositDate: TextView = itemView.findViewById(R.id.textDepositDate)
            val textDepositAmount: TextView = itemView.findViewById(R.id.textDepositAmount)
        }

        inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textTransactionDate: TextView = itemView.findViewById(R.id.textTransactionDate)
            val textProductName: TextView = itemView.findViewById(R.id.textProductName)
            val textProductCount: TextView = itemView.findViewById(R.id.textProductCount)
            val textUnitPrice: TextView = itemView.findViewById(R.id.textUnitPrice)
            val textAmountReceived: TextView = itemView.findViewById(R.id.textRecievedAmount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                DEPOSIT_VIEW_TYPE -> {
                    val depositView = inflater.inflate(R.layout.row_deposit, parent, false)
                    DepositViewHolder(depositView)
                }

                TRANSACTION_VIEW_TYPE -> {
                    val transactionView = inflater.inflate(R.layout.row_transaction, parent, false)
                    TransactionViewHolder(transactionView)
                }

                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            when (holder) {
                is DepositViewHolder -> {
                    // 입금용 데이터 설정
                    val depositItem = item as DepositItem
                    holder.textDepositAmount.text = formatAmount(depositItem.depositAmount)
                    holder.textDepositDate.text = depositItem.depositDate

                    // 카드뷰를 클릭했을 때 수정 다이얼로그를 띄우도록 리스너 추가
                    holder.itemView.setOnClickListener {
                        showModifyDialog(holder)
                    }
                }

                is TransactionViewHolder -> {
                    // 거래용 데이터 설정
                    val transactionItem = item as TransactionItem
                    holder.textTransactionDate.text = transactionItem.transactionDate
                    holder.textProductName.text = transactionItem.productName
                    holder.textProductCount.text = transactionItem.productCount
                    holder.textUnitPrice.text = formatAmount(transactionItem.unitPrice)
                    holder.textAmountReceived.text = formatAmount(transactionItem.amountReceived)

                    // 카드뷰를 클릭했을 때 수정 다이얼로그를 띄우도록 리스너 추가
                    holder.itemView.setOnClickListener {
                        showModifyDialog(holder)
                    }
                }
            }
        }

        // 수정 다이얼로그 설정
        private fun showModifyDialog(holder: RecyclerView.ViewHolder) {
            val dialog = Dialog(requireContext())

            when (holder) {
                is DepositViewHolder -> {
                    dialog.setContentView(R.layout.dialog_modify_deposit)
                    // 입금 데이터를 수정하는 로직을 구현
                }

                is TransactionViewHolder -> {
                    dialog.setContentView(R.layout.dialog_modify_transaction)
                    // 거래 데이터를 수정하는 로직을 구현
                }
            }
            dialog.show()
        }

        // 금액을 000,000 형식으로 변환하는 함수
        private fun formatAmount(amount: String): String {
            val longAmount = amount.toLongOrNull() ?: 0 // 금액을 Long으로 변환하고 null이면 0으로 처리
            val formattedAmount = String.format("%,d", longAmount)
            return "$formattedAmount"
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun getItemViewType(position: Int): Int {
            val item = items[position]
            return when (item) {
                is DepositItem -> DEPOSIT_VIEW_TYPE
                is TransactionItem -> TRANSACTION_VIEW_TYPE
                else -> throw IllegalArgumentException("Invalid item type")
            }
        }

        // 입금용 데이터를 추가하는 메서드
        fun addDeposit(deposit: DepositItem) {
            items.add(deposit)
            notifyDataSetChanged()
        }

        // 거래용 데이터를 추가하는 메서드
        fun addTransaction(transaction: TransactionItem) {
            items.add(transaction)
            notifyDataSetChanged()
        }
    }
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