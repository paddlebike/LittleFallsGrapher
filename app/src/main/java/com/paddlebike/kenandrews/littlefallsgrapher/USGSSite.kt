package com.paddlebike.kenandrews.littlefallsgrapher

import android.util.Log
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.joda.time.DateTime
import java.net.URL
import java.util.*


class GaugeConstants {
    companion object {
        const val GAUGE_TEMP  = "00010"
        const val GAUGE_LEVEL = "00065"
        const val GAUGE_FLOW  = "00060"

        const val ERROR_VALUE = -999999.00F
    }
}

private const val TAG = "USGSSite"
/**
 * Class for fetching USGS stream gauge data
 */
class USGSSite {

    companion object {
        fun fetchToLineGraphSeries(siteId: String): Set<LineGraphSeries<DataPoint>> {
            Log.d(TAG, "Fetching the gauge data for: $siteId")

            val url = "https://waterservices.usgs.gov/nwis/iv/?" +
                    "&period=P1D" +
                    "&format=json" +
                    "&parameterCd=${GaugeConstants.GAUGE_FLOW}" +
                    "&sites=$siteId"

            val response = URL(url).openStream()
            val jsonObject = Parser().parse(response) as? JsonObject ?:
            throw ExceptionInInitializerError("No response data")

            val value = jsonObject["value"] as? JsonObject ?:
            throw ExceptionInInitializerError("No value in response data")

            val timeSeries = value["timeSeries"] as? JsonArray<JsonObject> ?:
            throw ExceptionInInitializerError("No time series in response data")

            val siteSeries = mutableSetOf<LineGraphSeries<DataPoint>>()

            for (site in timeSeries) {
                val sourceInfo = site["sourceInfo"] as JsonObject
                val gaugeName = sourceInfo["siteName"] as String

                val siteData = site["values"] as? JsonArray<JsonObject>
                        ?: throw ExceptionInInitializerError("No values in response data")

                val gauges = siteData[0]["value"] as JsonArray<JsonObject>

                val dataPoints = mutableSetOf<DataPoint>()
                for (item in gauges) {
                    val reading = item["value"] as String
                    val time = DateTime(item["dateTime"] as String)
                    val date: Date = (time.toDate())
                    Log.d(TAG, "$gaugeName $date: $reading")
                    dataPoints.add(DataPoint(date, reading.toDouble()))
                }
                val series = LineGraphSeries<DataPoint>(dataPoints.toTypedArray())
                series.title = gaugeName
                siteSeries.add(series)
            }
            return siteSeries
        }
    }
}
