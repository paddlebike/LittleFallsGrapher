package com.paddlebike.kenandrews.littlefallsgrapher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.myToolbar))
        fetchGauge()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.refresh -> {
            fetchGauge()
            true
        }

        R.id.configure -> {
            val intent = Intent(this, ConfigurationActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun fetchGauge() {
        doAsync {
            try {
                val sites = USGSSite.fetchToLineGraphSeries("01646500,01638500,01618000,01636500,01646000")
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
        graph.removeAllSeries()
        val colorList = intArrayOf(Color.GREEN, Color.BLUE, Color.MAGENTA, Color.RED, Color.DKGRAY)
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
