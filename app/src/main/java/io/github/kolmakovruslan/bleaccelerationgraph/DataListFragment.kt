package io.github.kolmakovruslan.bleaccelerationgraph

import android.app.Fragment
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.data_list_fragment.*
import java.io.File
import java.util.ArrayList

/**
 * Created by 1 on 25.09.2017.
 */
class DataListFragment: Fragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.data_list_fragment, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DataListAdapter({ path ->
            (activity as MainActivity).navigateToData(path)
        })
        rvDataList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvDataList.adapter = adapter
        val allFiles = try {
            (reportDir().listFiles()
                    .filter { file -> file.length() != 0L }
                    .sortedByDescending(File::lastModified)).toList()}
        catch (e: Exception){
            emptyList<File>()
        }
        adapter.setNewData((allFiles.map {
            ModelRWData(it.lastModified().toString(), it.path.toString())
        }) as ArrayList<ModelRWData>)
    }



    private fun reportDir(): File {
        val dir = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "BleGraph")
        if (!dir.exists()) dir.mkdir()
        return dir
    }



}