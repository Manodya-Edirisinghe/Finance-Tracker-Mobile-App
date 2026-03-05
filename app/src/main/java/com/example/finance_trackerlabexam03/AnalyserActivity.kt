package com.example.finance_trackerlabexam03

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finance_trackerlabexam03.databinding.ActivityAnalyserBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AnalyserActivity : BaseActivity() {
    private lateinit var binding: ActivityAnalyserBinding
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategoryList()
        setupPieChart()
        loadData()
        setupBottomNavigation()
        updateBottomNavigationState()
    }

    private fun setupToolbar() {
        binding.titleText.text = "Expense Analyser"
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

    private fun setupCategoryList() {
        categoryAdapter = CategoryAdapter()
        binding.categoryList.apply {
            layoutManager = LinearLayoutManager(this@AnalyserActivity)
            adapter = categoryAdapter
        }
    }

    private fun loadData() {
        // Load data from SharedPreferences
        val sharedPreferences = getSharedPreferences("FinanceTracker", MODE_PRIVATE)
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        
        // Debug logging for raw JSON
        android.util.Log.d("AnalyserActivity", "Raw transactions JSON: $transactionsJson")
        
        val type = object : TypeToken<List<Transaction>>() {}.type
        val transactions = Gson().fromJson<List<Transaction>>(transactionsJson, type)

        // Debug logging for loaded transactions
        android.util.Log.d("AnalyserActivity", "Loaded ${transactions.size} transactions")
        transactions.forEach { transaction ->
            android.util.Log.d("AnalyserActivity", "Transaction: type=${transaction.type}, category=${transaction.category}, amount=${transaction.amount}")
        }

        // Get all categories from resources
        val allCategories = resources.getStringArray(R.array.transaction_categories).toList()
        
        // Define custom colors for each category
        val categoryColors = getCategoryColors()
        
        // Filter expenses and group by category
        val expensesByCategory = transactions
            .filter { it.type.equals("Expense", ignoreCase = true) }
            .groupBy { it.category }
            .mapValues { (_, transactions) -> 
                transactions.sumOf { it.amount }
            }

        // Create entries for all categories, including those with zero expenses
        val entries = allCategories.map { category ->
            val amount = expensesByCategory[category] ?: 0.0
            PieEntry(amount.toFloat(), category)
        }

        // Debug logging for expense categories
        android.util.Log.d("AnalyserActivity", "Found ${expensesByCategory.size} expense categories")
        expensesByCategory.forEach { (category, amount) ->
            android.util.Log.d("AnalyserActivity", "Category: $category, Amount: $amount")
        }

        if (entries.all { it.value == 0f }) {
            android.util.Log.d("AnalyserActivity", "No expense data found, showing empty state")
            binding.noDataText.visibility = android.view.View.VISIBLE
            binding.pieChart.visibility = android.view.View.GONE
            binding.categoryList.visibility = android.view.View.GONE
        } else {
            android.util.Log.d("AnalyserActivity", "Found expense data, updating UI")
            binding.noDataText.visibility = android.view.View.GONE
            binding.pieChart.visibility = android.view.View.VISIBLE
            binding.categoryList.visibility = android.view.View.VISIBLE

            // Update pie chart
            val totalExpense = entries.sumOf { it.value.toDouble() }
            android.util.Log.d("AnalyserActivity", "Total expense: $totalExpense")
            
            android.util.Log.d("AnalyserActivity", "Created ${entries.size} pie chart entries")
            
            val dataSet = PieDataSet(entries, "Expenses by Category").apply {
                // Set custom colors for each category
                colors = entries.map { entry -> 
                    categoryColors[entry.label] ?: Color.GRAY
                }
                valueTextSize = 12f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(binding.pieChart)
                setDrawValues(true)
                sliceSpace = 2f  // Add some space between slices
            }
            
            val pieData = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(binding.pieChart))
                setValueTextSize(12f)
                setValueTextColor(Color.WHITE)
            }
            
            binding.pieChart.apply {
                data = pieData
                setUsePercentValues(true)
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
                
                invalidate()
            }

            // Update category list with matching colors
            val categoryItems = entries
                .map { entry -> 
                    CategoryItem(
                        entry.label, 
                        entry.value.toDouble(),
                        categoryColors[entry.label] ?: Color.GRAY
                    )
                }
                .sortedByDescending { it.amount }
            
            categoryAdapter.submitList(categoryItems)
        }
    }

    private fun getCategoryColors(): Map<String, Int> {
        val colors = listOf(
            Color.parseColor("#2C3E50"), // Midnight Blue
            Color.parseColor("#E74C3C"), // Alizarin Red
            Color.parseColor("#27AE60"), // Emerald
            Color.parseColor("#8E44AD"), // Wisteria
            Color.parseColor("#2980B9"), // Belize Hole Blue
            Color.parseColor("#F39C12"), // Orange Sunflower
            Color.parseColor("#16A085"), // Greenish Cyan
            Color.parseColor("#C0392B"), // Strong Red
            Color.parseColor("#D35400"), // Pumpkin
            Color.parseColor("#7F8C8D")  // Concrete Gray
        )
        
        // Get all categories from resources
        val categories = resources.getStringArray(R.array.transaction_categories)
        
        // Create a map of category names to colors
        return categories.mapIndexed { index, category ->
            category to colors[index % colors.size]
        }.toMap()
    }
}

data class CategoryItem(
    val category: String,
    val amount: Double,
    val color: Int
)

class CategoryAdapter : androidx.recyclerview.widget.ListAdapter<CategoryItem, CategoryAdapter.CategoryViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = com.example.finance_trackerlabexam03.databinding.ItemCategoryAnalysisBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: com.example.finance_trackerlabexam03.databinding.ItemCategoryAnalysisBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryItem) {
            binding.categoryName.text = item.category
            binding.amount.text = String.format("$%.2f", item.amount)
            binding.categoryName.setTextColor(item.color)
        }
    }
} 