package com.paddlebike.kenandrews.littlefallsgrapher

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchGauge()
    }

    private fun fetchGauge() {
        doAsync {
            try {
                val sites = USGSSite.fetchToLineGraphSeries("01646500,01638500,01618000,01636500")
                uiThread {
                    Log.d("MainActivity", "Back on the UI thread")
                    updateGraph(sites)
                }
            }catch (e: Exception) {
                Log.e("MAIN", e.toString())
            }
        }
    }

    private fun updateGraph(sites: Set<LineGraphSeries<DataPoint>>) {
        val colorList = intArrayOf(Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED)
        var colorIndex = 0
        val graph = findViewById<View>(R.id.graph) as GraphView
        for (site in sites) {
            site.color = colorList[colorIndex]
            graph.addSeries(site)
            colorIndex += 1 % colorList.size
        }
        graph.legendRenderer.isVisible = true
        graph.legendRenderer.backgroundColor = Color.TRANSPARENT
        graph.legendRenderer.setFixedPosition(0,0)
        //graph.title = gaugeData.title
        // set date label formatter
        //val dateFormatter = SimpleDateFormat("HH:mm", Locale.US)
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(applicationContext)
    }
}
