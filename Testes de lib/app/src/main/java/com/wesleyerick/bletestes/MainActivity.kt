package com.wesleyerick.bletestes

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.internal.RxBleLog
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.SingleSource
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import java.util.*


class MainActivity : AppCompatActivity() {

    var rxBleClient: RxBleClient? = null
    var scanSubscription: Disposable? = null

    var context: Context? = null

    val uuid = UUID.fromString("0000fbb0-0000-1000-8000-00805f9b34fb")

    val autoConnect = true

    private val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()



        rxBleClient = RxBleClient.create(this)

        scanSubscription = rxBleClient!!.scanBleDevices(
                ScanSettings
                        .Builder()
//                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build() // add filters if needed
        )
                .subscribe(
                        { scanResult ->
//                            println("=====> scanResult: ${scanResult.bleDevice.macAddress}, name: ${scanResult.bleDevice.name}")

                            if (true) {

                                println(
                                        "" +
                                                "=====> scanResult: ${scanResult.bleDevice.macAddress}, name: ${scanResult.bleDevice.name}" +
                                                "=====> scanRecord: (serviceUuids:" +
                                                " ${scanResult.scanRecord.serviceUuids}," +
                                                "advertiseFlags: ${scanResult.scanRecord.advertiseFlags}," +
                                                "deviceName:  ${scanResult.scanRecord.deviceName}," +
                                                "manufacturerSpecificData:  ${scanResult.scanRecord.manufacturerSpecificData}," +
                                                "serviceData:  ${scanResult.scanRecord.serviceData}," +
                                                "bytes:  ${scanResult.scanRecord.bytes}," +
                                                "bytes.indices:  ${scanResult.scanRecord.bytes.indices}," +
                                                "txPowerLevel:  ${scanResult.scanRecord.txPowerLevel})"
                                )

//                                connect(rxBleClient!!, scanResult.bleDevice.macAddress.toString())
                            }
                        }
                ) { throwable ->
                    println("=====> throwable: $throwable")
                }

//        scan()
//        flowDisposabled()
    }

//    val flowDisposable = rxBleClient!!.observeStateChanges()
//            .switchMap<Any> { state: RxBleClient.State? ->
//                when (state) {
//                    RxBleClient.State.READY ->                 // everything should work
//                        return@switchMap rxBleClient!!.scanBleDevices()
//                    RxBleClient.State.BLUETOOTH_NOT_AVAILABLE, RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED, RxBleClient.State.BLUETOOTH_NOT_ENABLED, RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> return@switchMap Observable.empty()
//                    else -> return@switchMap Observable.empty()
//                }
//            }
//            .subscribe(
//                    { rxBleScanResult: Any? -> }
//            ) { throwable: Throwable? -> }

    fun flowDisposabled(): Disposable{

        return rxBleClient!!.observeStateChanges()
                .switchMap<Any> { state: RxBleClient.State? ->
                    when (state) {
                        RxBleClient.State.READY ->                 // everything should work
                            return@switchMap rxBleClient!!.scanBleDevices()
                        RxBleClient.State.BLUETOOTH_NOT_AVAILABLE, RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED, RxBleClient.State.BLUETOOTH_NOT_ENABLED, RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> return@switchMap Observable.empty()
                        else -> return@switchMap Observable.empty()
                    }
                }
                .subscribe(
                        { rxBleScanResult: Any? -> }
                ) { throwable: Throwable? -> }

    }

    fun requestPermissions(){
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    fun scan(){
        scanSubscription
    }

    fun connect(rxBleClient: RxBleClient, macAddress: String){

        scanSubscription!!.dispose()

        val device: RxBleDevice = rxBleClient.getBleDevice(macAddress)

        RxBleClient.setLogLevel(RxBleLog.DEBUG);


        val disposable = device.establishConnection(false) // <-- autoConnect flag
            .subscribe(
                    { rxBleConnection: RxBleConnection ->

//                        if (device.connectionState.toString() == "CONNECTED") {
//                            read(device)
//
//                            println("=====> connect / device.connectionState.toString(): CONNECTED")
//                        }

                        read(device)

                        println("=====>connect " +
                                "rxBleConnection: ${rxBleConnection}, " +
                                "rxBleConnection mtu: ${rxBleConnection.mtu}, " +
                                "state: ${rxBleClient.state}, " +
                                "bondedDevices: ${rxBleClient.bondedDevices}," +
                                "backgroundScanner: ${rxBleClient.backgroundScanner}")

                        statusConnection(device)
                    }
            ) { throwable: Throwable? -> connectToDevice(device.macAddress)
                println("=====> connect throwable" +
                    "message: ${throwable?.message}," +
                    "cause: ${throwable?.cause}," +
                    "localizedMessage: ${throwable?.localizedMessage}," +
                    "state: ${rxBleClient.state}," +
                    "bondState: ${device.bluetoothDevice.bondState}"
            ) }

//        if (device.connectionState){
            disposable
//        }

//        statusConnection(device)
    }

    private fun statusConnection(device: RxBleDevice) {

        device.observeConnectionStateChanges()
                .subscribe(
                        { connectionState ->
                            println("=====> CONNECTION STATE connectionState= $connectionState")
                        }
                ) { throwable ->
                    println("=====> CONNECTION STATE throwable= $throwable")
                }
    }

    fun connectToDevice(macAdress: String){
        //
        //// When done, just dispose.
        //flowDisposable.dispose();
        //    conection
        val mac = macAdress
        val device = rxBleClient!!.getBleDevice(mac)

        val disposable = device.establishConnection(autoConnect) // <-- autoConnect flag
                .subscribe(
                        { rxBleConnection: RxBleConnection? ->
                            println("=====> connectToDevice: $rxBleConnection")
                        }
                ) { throwable: Throwable? ->
                    println("=====> connectToDevice: throwable: $throwable")
                }

        disposable

    }

    fun read(device: RxBleDevice) {
        println("=====> read: called")
        device.establishConnection(autoConnect)
                .flatMapSingle(Function<RxBleConnection, SingleSource<out ByteArray>> { rxBleConnection: RxBleConnection -> rxBleConnection.readCharacteristic(uuid) }) //                .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")))
                .subscribe(
                        { characteristicValue: ByteArray -> println("=====> read characteristicValue:$characteristicValue") }
                ) { throwable: Throwable -> println("=====> read throwable:$throwable") }
    }


}