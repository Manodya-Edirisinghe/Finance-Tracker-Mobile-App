package com.example.finance_trackerlabexam03

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finance_trackerlabexam03.databinding.ActivityAnalysisBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AnalysisActivity : BaseActivity() {
    private lateinit var binding: ActivityAnalysisBinding
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupExpenseList()
        setupPieChart()
        loadData()
    }

    private fun setupToolbar() {
        binding.titleText.text = "Expense Analysis"
        binding.backButton.setOnClickListener { finish() }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setDrawHoleEnabled(true)
            setHoleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            setHoleRadius(58f)
            setTransparentCircleRadius(61f)
            setDrawCenterText(true)
            centerText = "Expenses"
            setCenterTextSize(16f)
            setCenterTextColor(Color.BLACK)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400)
            
            // Enable legend
            legend.isEnabled = true
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.textSize = 12f
            legend.textColor = Color.BLACK
        }
    }

    private fun setupExpenseList() {
        expenseAdapter = ExpenseAdapter()
        binding.expenseList.apply {
            layoutManager = LinearLayoutManager(this@AnalysisActivity)
            adapter = expenseAdapter
        }
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("FinanceTracker", MODE_PRIVATE)
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        val transactions = Gson().fromJson<List<Transaction>>(transactionsJson, type)

        // Filter expenses and group by category
        val expensesByCategory = transactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> 
                transactions.sumOf { it.amount }
            }

        // Update expense list
        val expenseItems = expensesByCategory.map { (category, amount) ->
            ExpenseItem(category, amount)
        }
        expenseAdapter.submitList(expenseItems)

        // Update pie chart
        val totalExpense = expensesByCategory.values.sum()
        val entries = expensesByCategory.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }
        
        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            colors = ColorTemplate.MATERIAL_COLORS.asList()
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(binding.pieChart)
        }
        
        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }
        
        binding.pieChart.data = pieData
        binding.pieChart.invalidate()

        // Calculate total income and expenses
        val totalIncome = transactions
            .filter { it.type == "income" }
            .sumOf { it.amount }
        
        val totalExpenseAmount = transactions
            .filter { it.type == "expense" }
            .sumOf { it.amount }

        // Calculate total balance (income - expenses)
        val totalBalance = totalIncome - totalExpenseAmount

        binding.totalBalanceText.text = String.format("$%.2f", totalBalance)
        binding.totalExpenseText.text = String.format("-$%.2f", totalExpenseAmount)
    }
}

data class ExpenseItem(
    val category: String,
    val amount: Double
)

class ExpenseAdapter : androidx.recyclerview.widget.ListAdapter<ExpenseItem, ExpenseAdapter.ExpenseViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<ExpenseItem>() {
        override fun areItemsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = com.example.finance_trackerlabexam03.databinding.ItemExpenseBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpenseViewHolder(
        private val binding: com.example.finance_trackerlabexam03.databinding.ItemExpenseBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseItem) {
            binding.categoryName.text = item.category
            binding.amount.text = String.format("$%.2f", item.amount)
        }
    }
} 