package io.github.kolmakovruslan.bleaccelerationgraph

import android.app.Fragment
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.measuring_fragment.*
import java.io.File
import java.io.FileInputStream
import java.util.*


/**
 * Created by 1 on 25.09.2017.
 */
class DataFragment: Fragment() {
    companion object{
        fun newInstanse(path: String, date: String): DataFragment{
            val fragment = DataFragment()
            val args = Bundle()
            fragment.date = date
            fragment.filepath = path
            fragment.arguments = args
            return fragment
        }
    }

    private var date: String = ""
    private var filepath: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.measuring_fragment, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        byNav.setOnClickListener{
            activity.fragmentManager.popBackStack()
        }
        title.text = date
        btRefresh.visibility = View.GONE

        val allFiles = try {
            (reportDir().listFiles()
                    .filter { file -> file.length() != 0L }
                    .sortedByDescending(File::lastModified)).toList()}
        catch (e: Exception){
            emptyList<File>()
        }
        val dataSet = mapRawData(readFromFile(allFiles.find { it.path == filepath }))
        dataGraph.data = LineData(dataSet)
        dataGraph.invalidate()
    }

    private fun readFromFile(file: File?): ByteArray {
        val data = ByteArray(file?.length()?.toInt()?: 0)
        try {
            FileInputStream(file).read(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    private fun mapRawData(byte: ByteArray): LineDataSet {
        val ints = IntArray(byte.size /2 -1)
        for (i in (1 until byte.size /2)) {
            val index = i * 2
            ints[i - 1] = byte[index - 1].toInt().shl(8) + byte[index].toInt()
        }
        val yVals = ints.mapIndexed { index, value ->
            Entry(index.toFloat() * MeasuringFragment.DATA_RATE, value.toFloat() / MeasuringFragment.G_DIVIDER)
        }
        return LineDataSet(yVals, Date().toString())
    }

    private fun reportDir(): File {
        val dir = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "BleGraph")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

}