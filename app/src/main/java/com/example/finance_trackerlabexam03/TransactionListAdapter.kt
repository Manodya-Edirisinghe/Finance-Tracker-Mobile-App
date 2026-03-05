package com.example.finance_trackerlabexam03

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finance_trackerlabexam03.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionListAdapter(
    private val transactions: List<Transaction>,
    private val onTransactionClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionText: TextView = itemView.findViewById(R.id.transactionDescription)
        private val categoryText: TextView = itemView.findViewById(R.id.transactionCategory)
        private val dateText: TextView = itemView.findViewById(R.id.transactionDate)
        private val amountText: TextView = itemView.findViewById(R.id.transactionAmount)

        init {
            itemView.setOnClickListener {
                onTransactionClick(transactions[adapterPosition])
            }
        }

        fun bind(transaction: Transaction) {
            // Display description in bold if available
            if (transaction.note.isNotEmpty()) {
                descriptionText.text = transaction.note
                descriptionText.visibility = View.VISIBLE
            } else {
                descriptionText.visibility = View.GONE
            }

            categoryText.text = transaction.category
            
            // Try to parse the date in both formats
            val date = try {
                dateFormat.parse(transaction.date)
            } catch (e: Exception) {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
                } catch (e: Exception) {
                    null
                }
            }
            dateText.text = date?.let { dateFormat.format(it) } ?: transaction.date

            // Set amount with proper formatting
            val formattedAmount = if (transaction.type == "Income") {
                "+$${String.format("%.2f", transaction.amount)}"
            } else {
                "-$${String.format("%.2f", transaction.amount)}"
            }
            amountText.text = formattedAmount
        }
    }
} 