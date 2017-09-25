package io.github.kolmakovruslan.bleaccelerationgraph

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.select_mode_fragment.*
import android.content.Context.MODE_PRIVATE
import java.io.OutputStreamWriter


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        replaceFragment(SelectModeFragment())
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 0){
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun replaceFragment(fragment: Fragment,
                                backStack: Boolean = true,
                                tag: String? = null) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (backStack) { fragmentTransaction.addToBackStack(tag) }
        fragmentTransaction.replace(R.id.mainfragmentPlaceholder, fragment, tag)
        fragmentTransaction.commit()
    }

    fun navigateToMeasuring() {
        replaceFragment(MeasuringFragment())
    }

    fun navigateToDataList() {
        replaceFragment(DataListFragment())
    }

    fun navigateToData(path: String) {
        replaceFragment(DataFragment.newInstanse(path))
    }
}
