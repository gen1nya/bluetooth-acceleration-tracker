package io.github.kolmakovruslan.bleaccelerationgraph

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.pawegio.kandroid.inflateLayout
import kotlinx.android.synthetic.main.item_view_data.view.*
import org.jetbrains.anko.layoutInflater
import java.util.ArrayList

/**
 * Created by 1 on 25.09.2017.
 */
data class ModelRWData(
        val title: String,
        val path: String
)

class DataListAdapter(
        val onClick: (path: String) -> Unit
): RecyclerView.Adapter<DataListAdapter.ViewHolder>() {
    var data: ArrayList<ModelRWData> = arrayListOf()

    fun setNewData(newData: ArrayList<ModelRWData>){
        data = newData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.context.inflateLayout(R.layout.item_view_data))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        fun bind(item: ModelRWData) {
            itemView.tvTitle.text = item.title
            itemView.setOnClickListener {
                onClick.invoke(item.path)
            }
        }
    }
}