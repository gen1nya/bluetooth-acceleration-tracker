package io.github.kolmakovruslan.bleaccelerationgraph

import android.widget.SimpleAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import org.jetbrains.anko.*
import java.util.*


class MainUi : AnkoComponent<MainActivity> {

    private lateinit var chart: LineChart
    private lateinit var ankoCtx: AnkoContext<MainActivity>

    private val measurements = mutableListOf<LineDataSet>()

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui.owner) {
        ankoCtx = ui
        verticalLayout {
            /*spinner {
                adapter = SimpleAdapter(context, measurements, R.layout.support_simple_spinner_dropdown_item, )
            }*/
            chart = LineChart(context).lparams(matchParent, matchParent)
            addView(chart)
        }
    }

    fun showData(byte: ByteArray) {
        ankoCtx.toast("Пришли новые данные")
        val dataSet = mapRawData(byte)
        measurements.add(dataSet)
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun mapRawData(byte: ByteArray): LineDataSet {
        val ints = IntArray(256)
        for (i in (1..256)) {
            val index = i * 2
            ints[i - 1] = byte[index - 1].toInt().shl(8) + byte[index].toInt()
        }
        val yVals = ints.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }
        return LineDataSet(yVals, Date().toString())
    }
}