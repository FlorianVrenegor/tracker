package com.example.tracker.weight

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tracker.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WeightFragment : Fragment(R.layout.fragment_weight) {

    private var adapter: WeightAdapter? = null
    private var weightViewModel: WeightViewModel? = null
    private lateinit var lineChartView: LineChartView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            weightViewModel!!.loadWeights()
        }

        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.fab)
        floatingActionButton.setOnClickListener {
            showAddWeightDialog()
        }

        adapter = WeightAdapter()

        val weightListView = view.findViewById<ListView>(R.id.weight_list_view)
        weightListView.isNestedScrollingEnabled = true
        weightListView.adapter = adapter
        weightListView.onItemLongClickListener = OnItemLongClickListener { _, _, position: Int, _ ->
            showDeleteWeightDialog(position)
            false
        }

        weightViewModel = ViewModelProvider(this)[WeightViewModel::class.java]
        weightViewModel!!.getWeights().observe(viewLifecycleOwner, { weights: List<WeightDto?>? ->
            adapter!!.setWeights(weights)
            swipeRefreshLayout.isRefreshing = false
        })
        weightViewModel!!.loadWeights()

        // Find the custom LineChartView
        lineChartView = view.findViewById(R.id.lineChart)

        // Example data points
        lineChartView.setDataPoints(listOf(10f, 20f, 15f, 25f, 30f, 20f, 40f))
    }

    private fun showAddWeightDialog() {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED

        val layoutName = LinearLayout(context)
        layoutName.orientation = LinearLayout.VERTICAL
        layoutName.setPadding(60, 0, 60, 0)
        layoutName.addView(input)

        val adb = AlertDialog.Builder(context!!)
        adb.setTitle("Gewicht eingeben")
        adb.setView(layoutName)
        adb.setPositiveButton("OK") { _, _ ->
            val weightString = input.text.toString()
            if (weightString.isNotEmpty()) {
                weightViewModel!!.saveWeight(WeightDto(System.currentTimeMillis(), weightString.toDouble()))
            }
        }
        adb.setNegativeButton("Abbrechen") { dialog, _ -> dialog.cancel() }
        adb.show()
    }

    private fun showDeleteWeightDialog(position: Int) {
        val adb = AlertDialog.Builder(activity!!)
        adb.setTitle("Delete?")
        adb.setMessage("Are you sure you want to delete this entry?")
        adb.setNegativeButton("Cancel", null)
        adb.setPositiveButton("Ok") { _, _ ->
            weightViewModel!!.deleteWeight(adapter!!.weights[position])
            adapter!!.weights.removeAt(position)
            adapter!!.notifyDataSetChanged()
        }
        adb.show()
    }
}