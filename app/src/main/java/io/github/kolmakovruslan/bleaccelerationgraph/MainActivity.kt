package io.github.kolmakovruslan.bleaccelerationgraph

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log.d
import org.jetbrains.anko.setContentView


class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothManager: BluetoothManager

    val ui = MainUi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        ui.setContentView(this)
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
        device.connectGatt(applicationContext, false, object : BluetoothGattCallback() {

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
                        ui.showData(characteristic.value)
                    }
                } else {
                    d("Connect", "onCharacteristicRead")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                val char = gatt?.services?.find { it.uuid.toString() == "0000ffff-0000-1000-8000-00805f9b34fb" }?.characteristics?.get(0)
                if (characteristic != null && characteristic.uuid.toString() == "0000ff00-0000-1000-8000-00805f9b34fb") {
                    gatt?.readCharacteristic(char)
                } else {
                    d("Connect", "onCharacteristicRead")
                }
            }
        })
    }
}
