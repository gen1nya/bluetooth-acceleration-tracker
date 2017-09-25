package io.github.kolmakovruslan.bleaccelerationgraph

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.select_mode_fragment.*

/**
 * Created by 1 on 25.09.2017.
 */
class SelectModeFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.select_mode_fragment, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btGotoMeasuring.setOnClickListener {
            (activity as MainActivity).navigateToMeasuring()
        }
        btGotoSaved.setOnClickListener {
            (activity as MainActivity).navigateToDataList()
        }
    }
}