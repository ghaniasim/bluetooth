package com.example.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanResults: HashMap<String, ScanResult>? = null
    private var mScanCallback: BtleScanCallback? = null
    private var mBluetoothLeScanner : BluetoothLeScanner? = null
    private var mHandler: Handler? = null
    private var mScanning: Boolean = false

    companion object {
        const val SCAN_PERIOD: Long = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        if (hasPermissions()) {
            btnScan.setOnClickListener { startScan() }
        }
    }

    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
            return true // assuming that the user grants permission
        }
        return true
    }

    private fun startScan() {
        Log.d("DBG", "Scan start")
        mScanResults = HashMap()
        mScanCallback = BtleScanCallback()
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner

        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

        val filter: List<ScanFilter>? = null

        // Stop scanning after a pre-defined scan period.
        mHandler = Handler()
        mHandler!!.postDelayed({stopScan()}, SCAN_PERIOD)

        mScanning = true
        mBluetoothLeScanner!!.startScan(filter, settings, mScanCallback)
    }

    private fun stopScan() {
        mBluetoothLeScanner!!.stopScan(mScanCallback)
        scanListView.adapter = myAdapter(this, GlobalModel.devices)
    }

    private inner class BtleScanCallback: ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
                addScanResult(result)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d("DBG", "BLE scan failed with code $errorCode")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceName = device.name
            val deviceAddress = device.address
            val signal = result.rssi

            mScanResults!![deviceAddress] = result
            Log.d("DBG", "Device name: $deviceName Device address: $deviceAddress")
            //Log.d("DBG", "Device address: $deviceAddress (${result.isConnectable})")

            if (deviceName == null){
                GlobalModel.devices.add(Device("Unnamed", deviceAddress, signal))
            } else {
                GlobalModel.devices.add(Device(deviceName, deviceAddress, signal))
            }
        }
    }
}