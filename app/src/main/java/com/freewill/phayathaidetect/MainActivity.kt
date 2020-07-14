package com.freewill.phayathaidetect

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.freewill.phayathaidetect.clients.RetrofitManager
import com.freewill.phayathaidetect.config.StringKeyData
import com.freewill.phayathaidetect.extension.getIMEI
import com.freewill.phayathaidetect.model.UpdateList2
import com.freewill.phayathaidetect.model.listFromServer.Body
import com.freewill.phayathaidetect.model.listFromServer.ListResponse
import com.freewill.phayathaidetect.model.updatelist.Androidbox
import com.fxn.stash.Stash
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import fwg.mdc.btc.nursetrackingtest.model.Itag
import fwg.mdc.btc.nursetrackingtest.model.ListItem
import fwg.mdc.btc.nursetrackingtest.model.UpdateListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kevinvista.bluetoothscanner.Device
import me.kevinvista.bluetoothscanner.DeviceAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList

//class MainActivity : AppCompatActivity() {
class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    var SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
    private val ACCESS_COARSE_LOCATION_CODE = 1

    private val REQUEST_ENABLE_BLUETOOTH = 2

    private val SCAN_MODE_ERROR = 3

    private var bluetoothReceiverRegistered: Boolean = false

    private var scanModeReceiverRegistered: Boolean = false

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null

    private var recyclerView: RecyclerView? = null

    private var deviceAdapter: DeviceAdapter? = null

    private var devices: ArrayList<Device>? = ArrayList()

    private var checkBluetoothStop = true

    private var callCount = 1

    var bluetoothScanner : BluetoothLeScanner? = null

    var bluetoothLeAdvertiser : BluetoothLeAdvertiser? = null

    val pUuid = UUID.fromString("17E033D3-490E-4BC9-9FE8-2F567643F4D3")
    var charLength = 3
    val randomUUID = UUID.randomUUID().toString()
    val finalString = randomUUID.substring(randomUUID.length - charLength, randomUUID.length)
    val serviceDataByteArray = finalString.toByteArray()

    //for advertise
    val adverTiseSettings = AdvertiseSettings.Builder()
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        .setConnectable(true)
        .setTimeout(0)
        .build()

    val data = AdvertiseData.Builder()
        .setIncludeDeviceName(false)
        .setIncludeTxPowerLevel(true)
        .addServiceUuid(ParcelUuid(pUuid))
        .addManufacturerData(1023, serviceDataByteArray)
        .build()

    private val handler = Handler()
    val gson = Gson()

    var updateListResponse = UpdateListResponse()
    var filteredDeviceList : com.freewill.phayathaidetect.model.Body? = null
    var filteredDeviceListVersion : String? = null

    internal var scanTask: Runnable = object : Runnable {
        override fun run() {
            GlobalScope.launch(context = Dispatchers.Main) {
                repeat(5000) {
                    if (mBluetoothAdapter != null) {
                        scanBluetooth()
                    }else {
                        Toast.makeText(this@MainActivity, "no bluetooth", Toast.LENGTH_SHORT).show()
                    }
                    delay(3000)
                    devices?.clear()
                }
            }
        }
    }
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "onReceive: Execute")
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                val manuName = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_NAME)

                val deviceName = device.name
                val paired = device.bondState == BluetoothDevice.BOND_BONDED
                val deviceAddress = device.address
                val deviceUUID = device.uuids
                val deviceRSSI = intent.extras!!.getShort(BluetoothDevice.EXTRA_RSSI, 0.toShort())
                var uuidString = intent.getStringExtra(BluetoothDevice.EXTRA_UUID)
                Log.e("uuuuiiiidddd", "name: ${device.name}\ndistnace:  ${uuidString}")
                var distance = 2/-63.52 * deviceRSSI.toDouble()
                val distanceTX = calculateDistance(.001,deviceRSSI.toDouble())
                Log.e("distancexx", "name: ${device.name}\ndistnace:  ${distance}")
                Log.e("distancexx", "name: ${device.name}\ndistnaceTX:  ${distanceTX}")

                //for get uuid and characteristic
//                device.connectGatt(this@MainActivity, false, gattCallback).connect()

                deviceUUID?.forEach {
                    uuidString = it.uuid.toString()
                }
                val mDevice = Device(deviceName, paired, deviceAddress, deviceRSSI, uuidString)
//                devices?.remove(scannedDevice(mDevice))
//                 devices?.clear()
                devices?.add(mDevice)
                deviceAdapter?.notifyDataSetChanged()

                var updateList = UpdateListResponse()
                var androidbox = Androidbox()
                var itag = Itag()
                var listOfItag = ArrayList<ListItem>()

                devices?.forEach {
                    var foundDevice = it
                    filteredDeviceList?.itagList?.forEach {
                        if (foundDevice.address.equals(it?.macAddress)) {
                            listOfItag.add(
                                ListItem(
                                    foundDevice.signal?.toDouble(),
                                    foundDevice.address,
                                    foundDevice.UUID
                                )
                            )
                        }
                    }
                }

                itag.version = filteredDeviceListVersion
                itag.list = listOfItag
                androidbox.deviceId = "50321a567585340f"

                updateList.androidbox = androidbox
                updateList.itag = itag

                Log.e("devicelist", "${updateList}")

                /* send data to server */
               sendList(updateList)

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                if (devices?.size == 0) {
                    Log.e(TAG, "onReceive: No device")

                }
            }
        }

        private fun scannedDevice(d: Device): Device? {
            for (device in devices!!) {
                if (d.address == device.address) {
                    return device
                }
            }
            return null
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.e("advertiserrr", "${settingsInEffect}")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
//            Log.e("advertiserrr", "${errorCode}")

            var reason: String

            when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> {
                    reason = "ADVERTISE_FAILED_ALREADY_STARTED"
                }
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> {
                    reason = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                }
                ADVERTISE_FAILED_INTERNAL_ERROR -> {
                    reason = "ADVERTISE_FAILED_INTERNAL_ERROR"
                }
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> {
                    reason = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                }
                ADVERTISE_FAILED_DATA_TOO_LARGE -> {
                    reason = "ADVERTISE_FAILED_DATA_TOO_LARGE"
                }

                else -> {
                    reason = "UNDOCUMENTED"
                }
            }

            Log.e("advertiserrr", "Advertising onStartFailure: $errorCode - $reason")
        }
    }

    private val scanModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, SCAN_MODE_ERROR)
            if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE || scanMode == BluetoothAdapter.SCAN_MODE_NONE) {
                Toast.makeText(context, "อุปกรณ์มองไม่เห็นจากด้านนอก", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        contextOfApplication = applicationContext
        filteredDeviceList = Stash.getObject<Any>(StringKeyData.FILTER_LIST, com.freewill.phayathaidetect.model.Body::class.java) as com.freewill.phayathaidetect.model.Body?
        filteredDeviceListVersion = Stash.getString(StringKeyData.LIST_VERSION)

        Log.e("droiddroid", getIMEI(MainActivity@this))
        Log.e("stashlist", "${filteredDeviceList}")
        Log.e("stashlist", "${filteredDeviceListVersion}")
        initView()
        initData()
        handler.post(scanTask)

//        val pUuid = UUID.fromString("17E033D3-490E-4BC9-9FE8-2F567643F4D3")
//        var charLength = 3
//        val randomUUID = UUID.randomUUID().toString()
//        val finalString = randomUUID.substring(randomUUID.length - charLength, randomUUID.length)
//        val serviceDataByteArray = finalString.toByteArray()
//
//        //for advertise
//        val adverTiseSettings = AdvertiseSettings.Builder()
//            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
//            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
//            .setConnectable(true)
//            .setTimeout(600)
//            .build()
//
//        val data = AdvertiseData.Builder()
//            .setIncludeDeviceName(false)
//            .setIncludeTxPowerLevel(true)
//            .addServiceUuid(ParcelUuid(pUuid))
//            .addManufacturerData(1023, serviceDataByteArray)
//            .build()

        //for scan
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(pUuid))
            .build()

        val filters: ArrayList<ScanFilter> = ArrayList()
        filters.add(filter)

        val scanSettings = ScanSettings.Builder()
            .setReportDelay(0)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bluetoothLeAdvertiser?.startAdvertising(adverTiseSettings, data, advertiseCallback)
        bluetoothScanner?.startScan(null, scanSettings,scanCallback)
//        bluetoothScanner?.startScan(scanCallback)
        deviceAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        if (checkBluetoothStop) {
            checkBluetooth(true)
        }

        handler.post(scanTask)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(bluetoothReceiver)
            unregisterReceiver(scanModeReceiver)
        }catch (e :Exception) {

        }
        handler.removeCallbacks(scanTask)
    }


    override fun onRestart() {
        super.onRestart()
        handler.post(scanTask)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothReceiverRegistered) {
            unregisterReceiver(bluetoothReceiver)
        }
        if (scanModeReceiverRegistered) {
            unregisterReceiver(scanModeReceiver)
        }
//        unregisterReceiver(bluetoothReceiver)
//        unregisterReceiver(scanModeReceiver)
//        handler.removeCallbacks(scanTask)
        finish()
    }
    private fun initView() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh) as SwipeRefreshLayout
        swipeRefreshLayout!!.setColorSchemeResources(R.color.colorPrimary)
        swipeRefreshLayout!!.setOnRefreshListener(this)
        recyclerView = findViewById(R.id.recycler_view) as RecyclerView

        devices = ArrayList<Device>()
        deviceAdapter = DeviceAdapter(devices!!)
        recyclerView!!.adapter = deviceAdapter
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
    }

    private fun initData() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothScanner = mBluetoothAdapter?.getBluetoothLeScanner()
        bluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
    }

    fun scanBluetooth() {
        bluetoothReceiverRegistered = true
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.EXTRA_UUID)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(bluetoothReceiver, filter)
        if (mBluetoothAdapter?.isDiscovering!!) {
            mBluetoothAdapter?.cancelDiscovery()
        }
        mBluetoothAdapter?.startDiscovery()
    }

    override fun onRefresh() {
        runOnUiThread {
            if (mBluetoothAdapter != null) {
                if (!mBluetoothAdapter!!.isEnabled) {
                    mBluetoothAdapter?.enable()
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
                }
                handler.post(scanTask)
            }
            deviceAdapter!!.notifyDataSetChanged()
            swipeRefreshLayout!!.isRefreshing = false
        }
    }

    companion object {

        val TAG = "MainActivity"

        var contextOfApplication: Context? = null
    }

    private fun checkBluetooth(checkBluetooth: Boolean) {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                // Bluetooth is not enable :)

                if (checkBluetooth == true) {
                    var activity = this
                    if (activity != null) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
                    }

                }

            } else {

                requesLocationPermission()

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {

            // Usuario ha activado el bluetooth
            if (resultCode == Activity.RESULT_OK) {

                Log.e("REQUEST_BLUETOOTH", "REQUEST_ENABLE_BLUETOOTH Ok ")
                checkBluetoothStop = true
                requesLocationPermission()

            } else if (resultCode == Activity.RESULT_CANCELED) { // User refuses to enable bluetooth
                // context!!.toast(getString(R.string.no_bluetooth_msg))
                checkBluetoothStop = false

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun requesLocationPermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    if (report!!.areAllPermissionsGranted()) {
                        Toast.makeText(
                            this@MainActivity,
                            "All permissions are granted!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // check for permanent denial of any permission
                    if (report!!.isAnyPermissionPermanentlyDenied()) {
                        // show alert dialog navigating to Settings
                        //  showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                }

            }).check()
    }

    fun sendList(updateList : UpdateListResponse) {
        var call = RetrofitManager.getInstance().apiService.APIUpdateList(updateList)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.e("apiapiapi", "fail: ${t}")
                callCount += 1
            }

            override fun onResponse(
                call: Call<ResponseBody>?,
                response: Response<ResponseBody>?
            ) {
                var s = response?.body()!!.string()
                callCount += 1

                try {
                    val itemType = object : TypeToken<UpdateList2>() {}.type
                    val listFromServer = gson.fromJson<UpdateList2>(s, itemType)
                    Log.e("apiapiapi", "success ${s}")
                    Stash.put(StringKeyData.LIST_VERSION, listFromServer.head?.version)
                    if (!listFromServer.body?.itagList.isNullOrEmpty()) {
                        Stash.put(StringKeyData.FILTER_LIST, listFromServer?.body)
                        filteredDeviceList = listFromServer.body
                    }
                    filteredDeviceListVersion = listFromServer.head?.version
                    Toast.makeText(
                        this@MainActivity, "call count =${callCount}\n" +
                                "${listFromServer}"
                        , Toast.LENGTH_SHORT
                    ).show()
                }catch (e : Exception) {
                    Log.e("apiapiapi", "success ${e.message}")
                }
            }
        })
    }

    val scanCallback = object: ScanCallback(){
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            var device = result.device
            //224 is the official bluetooth google company identifier.
//            val manufacturerSpecificData = result.scanRecord?.getManufacturerSpecificData(224)
            val rssi = result.rssi.toDouble() //The intensity of the received signal
            val tx = result.txPower.toDouble() //The power of the broadcast (Available on Oreo only)
            val distance = calculateDistance(tx,rssi)
            var manuString = "can't get"
            try {
                val manufacturerSpecificData = result.scanRecord?.getManufacturerSpecificData(1023)
                manuString = String(manufacturerSpecificData!!, Charsets.UTF_8)
            }catch (e: Exception) {

            }
            Log.e("distancefrom", "name: ${device.name}\nmanu:  ${manuString}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("distancefrom", "${errorCode}")
        }
    }

    fun calculateDistance(txPower: Double, rssi: Double): Double {
        val ratio = rssi / txPower
        if (rssi == 0.0) { // Cannot determine accuracy, return -1.
            return -1.0
        } else if (ratio < 1.0) { //default ratio
            return Math.pow(ratio, 10.0)
        }//rssi is greater than transmission strength
        return (0.89976) * Math.pow(ratio, 7.7095) + 0.111
    }

    val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.e("connectiongatt", "${gatt?.device?.name}")
            when (newState) {
                BluetoothGatt.STATE_CONNECTED ->
                    {
                        gatt?.discoverServices()
                    }
                BluetoothGatt.STATE_DISCONNECTED -> { /* do something when disconnected */ }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            val characteristicRawByteArray = characteristic?.value.toString()
            val characteristicStringValue = characteristic?.getStringValue(0)
            Log.e("characteristicss", "${characteristicStringValue}")

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            val uuid = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
            val uuid2 = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")
            val GENERIC_ACCESS_SERVICE = convertFromInteger(0x1800)
            val DEVICE_MANU_FAC = convertFromInteger(0x2A29)
            val DEVICE_NAME_CHARACTERISTIC = convertFromInteger(0x2A00)
            val genericAccessService = gatt?.getService(uuid)
            val deviceNameCharacteristic = genericAccessService?.getCharacteristic(DEVICE_NAME_CHARACTERISTIC)
            Log.e("servicesss", "${gatt?.device?.name}")
            gatt?.services?.forEach {
                Log.e("servicesss", " - ${it.instanceId}")
                it.characteristics.forEach {
                    Log.e("servicesss", "  - ${it.uuid}")
                }
//                Log.e("servicesss", " - ${it.getCharacteristic(DEVICE_MANU_FAC).uuid}")
            }

            //Now, gatt variable will contains services and characteristic of the device

            deviceNameCharacteristic?.writeType = BluetoothGattCharacteristic.PERMISSION_READ
            gatt?.readCharacteristic(deviceNameCharacteristic)

//            gatt?.disconnect()

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            val data = characteristic?.value
            Log.e("characteristicss", "change : ${data}")
        }
    }

    fun convertFromInteger(i: Int): UUID? {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and ((-0x1).toLong()).toInt()).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }
}
