package io.github.kolmakovruslan.bleaccelerationgraph

import android.app.Fragment
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.measuring_fragment.*
import org.jetbrains.anko.act
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by 1 on 25.09.2017.
 */
class MeasuringFragment: Fragment() {

    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.measuring_fragment, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if (!bluetoothManager.adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        bluetoothManager.adapter.startLeScan(object : BluetoothAdapter.LeScanCallback {
            override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                if (device != null && device.name == "I_LIKE_BLE") {
                    bluetoothManager.adapter.stopLeScan(this)
                    connect(device)
                }
            }
        })



    }

    private fun connect(device: BluetoothDevice) {
        device.connectGatt(activity.applicationContext, false, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt?.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val char = gatt?.services?.find { it.uuid.toString() == "0000ffff-0000-1000-8000-00805f9b34fb" }?.characteristics?.get(0)
                    val char_indicator = gatt?.services?.find { it.uuid.toString() == "0000fffa-0000-1000-8000-00805f9b34fb" }?.characteristics?.get(0)
                    if (char != null && char_indicator != null) {
                        gatt.setCharacteristicNotification(char_indicator, true)
                        gatt.readCharacteristic(char)
                    }
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                if (characteristic != null && status == BluetoothGatt.GATT_SUCCESS &&
                        characteristic.uuid.toString() == "0000ff01-0000-1000-8000-00805f9b34fb") {
                    runOnUiThread {
                        idPrograss.visibility = View.GONE
                        showData(characteristic.value)
                    }
                } else {
                    Log.d("Connect", "onCharacteristicRead")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                val char = gatt?.services?.find { it.uuid.toString() == "0000ffff-0000-1000-8000-00805f9b34fb" }?.characteristics?.get(0)
                if (characteristic != null && characteristic.uuid.toString() == "0000ff00-0000-1000-8000-00805f9b34fb") {
                    gatt?.readCharacteristic(char)
                    runOnUiThread {
                        idPrograss.visibility = View.VISIBLE
                    }

                } else {
                    Log.d("Connect", "onCharacteristicRead")
                }
            }
        })
    }

    private val measurements = mutableListOf<LineDataSet>()
    fun showData(byte: ByteArray) {
        toast("Новые данные")
        val dataSet = mapRawData(byte)
        writeToFile(byte)
        measurements.add(dataSet)
        dataGraph.data = LineData(dataSet)
        dataGraph.invalidate()
    }


    private fun writeToFile(data: ByteArray) {

    }

    private fun reportDir(): File? {
        val dir = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "BleGraph")
        if (!dir.exists()) dir.mkdir()
        return dir
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

