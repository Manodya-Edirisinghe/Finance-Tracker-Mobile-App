package com.example.finance_trackerlabexam03

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finance_trackerlabexam03.databinding.ItemTransactionBinding
import com.example.finance_trackerlabexam03.models.Transaction
import com.example.finance_trackerlabexam03.utils.CurrencyUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val context: Context,
    private val onTransactionClick: (Transaction, Int) -> Unit,
    private val onTransactionUpdated: () -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val gson = Gson()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FinanceTracker", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val transactions = mutableListOf<Transaction>()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        val typeToken = object : TypeToken<List<Transaction>>() {}.type
        val loadedTransactions = gson.fromJson<List<Transaction>>(transactionsJson, typeToken)
        transactions.clear()
        transactions.addAll(loadedTransactions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showTransactionDetailsDialog(context, transactions[position], position)
                }
            }
        }

        fun bind(transaction: Transaction) {
            binding.apply {
                transactionCategory.text = transaction.category
                transactionAmount.text = CurrencyUtils.formatAmount(context, transaction.amount)
                
                // Display description in bold if available
                val note = transaction.note
                if (!note.isNullOrEmpty()) {
                    transactionDescription.text = note
                    transactionDescription.visibility = View.VISIBLE
                } else {
                    transactionDescription.visibility = View.GONE
                }
                
                // Try to parse the date in both formats
                val dateFormat1 = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = try {
                    dateFormat1.parse(transaction.date)
                } catch (e: Exception) {
                    try {
                        dateFormat2.parse(transaction.date)
                    } catch (e: Exception) {
                        null
                    }
                }
                transactionDate.text = date?.let { dateFormat1.format(it) } ?: transaction.date

                // Set category icon based on transaction category
                val categoryIconRes = when (transaction.category.lowercase()) {
                    "food" -> R.drawable.food
                    "transport" -> R.drawable.transport
                    "medicine" -> R.drawable.medicine
                    "groceries" -> R.drawable.grocerry
                    "rent" -> R.drawable.rent
                    "gifts" -> R.drawable.gifts
                    "savings" -> R.drawable.savings
                    "entertainment" -> R.drawable.entertainment
                    "other" -> R.drawable.other
                    else -> R.drawable.other
                }
                transactionIcon.setImageResource(categoryIconRes)
                // Set icon tint to black
                transactionIcon.setColorFilter(android.graphics.Color.BLACK)

                // Set text color based on transaction type
                val textColor = if (transaction.type == "Income") {
                    context.getColor(R.color.income_green)
                } else {
                    context.getColor(R.color.expense_red)
                }
                transactionAmount.setTextColor(textColor)
            }
        }
    }

    fun updateTransactions() {
        loadTransactions()
    }

    private fun showTransactionDetailsDialog(
        context: Context,
        transaction: Transaction,
        position: Int
    ) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_transaction_details, null)
        
        // Set transaction details in dialog
        dialogView.findViewById<TextView>(R.id.dialogAmount).text = 
            CurrencyUtils.formatAmountWithSign(context, transaction.amount, transaction.type == "Income")
        dialogView.findViewById<TextView>(R.id.dialogCategory).text = transaction.category
        dialogView.findViewById<TextView>(R.id.dialogDate).text = transaction.date
        dialogView.findViewById<TextView>(R.id.dialogType).text = transaction.type

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // Handle delete button click
        dialogView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            deleteTransaction(position)
            dialog.dismiss()
        }

        // Handle update button click
        dialogView.findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            showUpdateTransactionDialog(context, transaction, position)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteTransaction(position: Int) {
        // Get current transactions
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        val typeToken = object : TypeToken<List<Transaction>>() {}.type
        val allTransactions = gson.fromJson<List<Transaction>>(transactionsJson, typeToken).toMutableList()
        
        // Remove the transaction
        allTransactions.removeAt(position)
        
        // Save updated transactions
        sharedPreferences.edit().putString("transactions", gson.toJson(allTransactions)).apply()
        
        // Update the adapter
        transactions.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, transactions.size)
        
        // Notify activity to update totals
        onTransactionUpdated()
    }

    private fun showUpdateTransactionDialog(
        context: Context,
        transaction: Transaction,
        position: Int
    ) {
        val intent = Intent(context, AddTransactionFormActivity::class.java).apply {
            putExtra("transaction", transaction as java.io.Serializable)
            putExtra("position", position)
        }
        context.startActivity(intent)
    }
} 