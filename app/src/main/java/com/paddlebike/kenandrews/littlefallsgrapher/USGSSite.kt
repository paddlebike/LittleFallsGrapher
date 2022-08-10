package com.paddlebike.kenandrews.littlefallsgrapher

import android.util.Log
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
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
        @OptIn(ExperimentalSerializationApi::class)
        fun fetchToLineGraphSeries(siteId: String): Set<LineGraphSeries<DataPoint>> {
            Log.d(TAG, "Fetching the gauge data for: $siteId")

            val url = "https://waterservices.usgs.gov/nwis/iv/?" +
                    "&period=P1D" +
                    "&format=json" +
                    "&parameterCd=${GaugeConstants.GAUGE_FLOW}" +
                    "&sites=$siteId"

            val response = URL(url).openStream()
            val json = Json.decodeFromStream(response) as JsonObject

            val value = json.getValue("value") as JsonObject

            val timeSeries = value.getValue("timeSeries") as JsonArray

            val siteSeries = mutableSetOf<LineGraphSeries<DataPoint>>()

            for (site in timeSeries) {
                val sourceInfo = site.jsonObject.getValue("sourceInfo") as JsonObject
                val gaugeNameJL = sourceInfo.getValue("siteName") as JsonPrimitive
                val gaugeName = gaugeNameJL.content
                Log.d(TAG, "Fetched gauge data for: $gaugeName")

                val siteData = site.jsonObject.getValue("values") as JsonArray

                val gauges = siteData[0].jsonObject.getValue("value") as JsonArray

                val dataPoints = mutableSetOf<DataPoint>()
                for (item in gauges) {
                    val readingJL = item.jsonObject.getValue("value") as JsonPrimitive
                    val reading = readingJL.double
                    val ds = item.jsonObject.getValue("dateTime") as JsonPrimitive
                    val time = DateTime(ds.content)
                    val date: Date = (time.toDate())
                    Log.d(TAG, "$gaugeName $ds: $reading")
                    dataPoints.add(DataPoint(date, reading))
                }
                val series = LineGraphSeries<DataPoint>(dataPoints.toTypedArray())
                series.title = gaugeName
                siteSeries.add(series)
            }
            return siteSeries
        }
    }
}
