package io.github.kolmakovruslan.bleaccelerationgraph

import android.app.Fragment
import android.bluetooth.*
import android.content.Context
import android.content.Intent
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
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by 1 on 25.09.2017.
 */
class MeasuringFragment: Fragment() {
    companion object {
        const val DATA_RATE = 10F
        const val G_DIVIDER = 1.3333334F
        private const val TAG = "MeasuringFragment"
        private const val DATA_SERVICE_UUID         = "0000ffff-0000-1000-8000-00805f9b34fb"
        private const val DATA_VALUE_UUID           = "0000ff01-0000-1000-8000-00805f9b34fb"
        private const val INDICATE_SERVICE_UUID     = "0000fffa-0000-1000-8000-00805f9b34fb"
        private const val INDICATOR_VALUE_UUID      = "0000ff00-0000-1000-8000-00805f9b34fb"
        private const val DEVICE_NAME = "I_LIKE_BLE"
        private const val REQUEST_ENABLE_BT = 1
    }

    private var currentFrame: Int = 0
    private var rawData: ArrayList<Byte> = arrayListOf()
    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.measuring_fragment, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btRefresh.setOnClickListener{
            enableSearchMode()
        }
        byNav.setOnClickListener{ activity.fragmentManager.popBackStack() }
        bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (!bluetoothManager.adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        title.text =  getString(R.string.searching)
        bluetoothManager.adapter.startLeScan(object : BluetoothAdapter.LeScanCallback {
            override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                if (device != null && device.name == DEVICE_NAME) {
                    bluetoothManager.adapter.stopLeScan(this)
                    connect(device)
                }
            }
        })

    }

    private fun enableSearchMode(){
        runOnUiThread {  title.text =  getString(R.string.searching) }
        bluetoothManager.adapter.startLeScan(object : BluetoothAdapter.LeScanCallback {
            override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                if (device != null && device.name == DEVICE_NAME) {
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
                    runOnUiThread { title.text = getString(R.string.connected) }
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    enableSearchMode()
                    runOnUiThread { title.text = getString(R.string.disconnected) }
                }
                if (newState == BluetoothProfile.STATE_CONNECTING){
                    runOnUiThread { title.text = getString(R.string.connecting) }
                }

            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val char_indicator = gatt?.services?.find { it.uuid.toString() == INDICATE_SERVICE_UUID }?.characteristics?.get(0)
                    if (char_indicator != null) {
                        gatt.setCharacteristicNotification(char_indicator, true)
                        gatt.readCharacteristic(char_indicator)
                    }
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                val char = gatt?.services?.find { it.uuid.toString() == DATA_SERVICE_UUID }?.characteristics?.get(0)
                if (characteristic != null && status == BluetoothGatt.GATT_SUCCESS){
                    when (characteristic.uuid.toString()){
                        INDICATOR_VALUE_UUID -> {
                            Log.i(TAG, "write: " + characteristic.value.first().toString())
                            if (characteristic.value.first() in 0..31) {
                                gatt?.readCharacteristic(char)
                            } else {
                                currentFrame = 0
                                rawData.clear()
                            }
                        }
                    }
                }
                Log.d(TAG, "onCharacteristicWrite")
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                if (characteristic != null && status == BluetoothGatt.GATT_SUCCESS){
                    if (characteristic.uuid.toString() == DATA_VALUE_UUID) {
                        Log.i(TAG, "current Frame is " + currentFrame.toString())
                        if (currentFrame in 0..31){
                            runOnUiThread { pbLoading.progress = currentFrame }
                            currentFrame++
                            val char_indicator = gatt?.services?.find { it.uuid.toString() == INDICATE_SERVICE_UUID }?.characteristics?.get(0)
                            char_indicator?.value = kotlin.ByteArray(1,{ currentFrame.toByte() })
                            gatt?.writeCharacteristic( char_indicator )
                        } else {
                            currentFrame = -1
                        }
                        rawData.addAll(characteristic.value.map { it })
                    }
                }
                Log.d(TAG, "onCharacteristicRead")
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                val char = gatt?.services?.find { it.uuid.toString() == DATA_SERVICE_UUID }?.characteristics?.get(0)
                if (characteristic != null && characteristic.uuid.toString() == INDICATOR_VALUE_UUID) {
                    if (characteristic.value.first() == 0xff.toByte()){
                        Log.i(TAG, "notify 0xff")
                        runOnUiThread {
                            title.text = getString(R.string.connected)
                            pbLoading.visibility = View.INVISIBLE
                            currentFrame = -1
                            showData(rawData.toByteArray())
                            rawData.clear()
                        }
                    }
                    if (characteristic.value.first() == 0x00.toByte()){
                        currentFrame = 0x00
                        Log.i(TAG, "notify 0x00")
                        runOnUiThread {
                            pbLoading.visibility = View.VISIBLE
                            title.text = getString(R.string.receiving)
                        }
                        gatt?.readCharacteristic(char)
                    }
                } else {
                    Log.d(TAG, "onCharacteristicRead")
                }
            }
        })
    }

    fun showData(byte: ByteArray) {
        val dataSet = mapRawData(byte)
        writeToFile(byte)
        dataGraph.data = LineData(dataSet)
        dataGraph.invalidate()
    }


    private fun writeToFile(data: ByteArray) {
        val file = File(getDir(), System.currentTimeMillis().toString())
        val outputStream = FileOutputStream(file)
        try {
            outputStream.write(data)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            toast(getString(R.string.file_write_error))
        }

    }

    private fun getDir(): File? {
        val dir = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + getString(R.string.dir_name))
        if (!dir.exists()) dir.mkdir()
        return dir
    }

    private fun mapRawData(byte: ByteArray): LineDataSet {
        val ints = IntArray(byte.size /2 -1)
        for (i in (1..(byte.size /2 -1))) {
            val index = i * 2
            ints[i - 1] = byte[index - 1].toInt().shl(8) + byte[index].toInt()
        }
        val yVals = ints.mapIndexed { index, value ->
            Entry(index.toFloat() * DATA_RATE , value.toFloat() / G_DIVIDER)
        }
        return LineDataSet(yVals, Date().toString())
    }

}

