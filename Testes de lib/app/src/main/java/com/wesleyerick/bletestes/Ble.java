package com.wesleyerick.bletestes;

import android.content.Context;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
public class Ble {

    Context context;

    RxBleClient rxBleClient;

    public void setContext(Context context){
        this.context = context;
        rxBleClient = RxBleClient.create(this.context);
    }

    UUID characteristicUuid = UUID.fromString("0000fbb0-0000-1000-8000-00805f9b34fb");

    Boolean autoConnect = true;

//    Discovery
    Disposable scanSubscription = rxBleClient.scanBleDevices(
            new ScanSettings.Builder()
                    // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                    // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                    .build()
            // add filters if needed
    )
            .subscribe(
                    scanResult -> {
                        // Process scan result here.
                    },
                    throwable -> {
                        // Handle an error here.
                    }
            );

        //// When done, just dispose.
        //scanSubscription.dispose();

//    Status do cliente


    Disposable flowDisposable = rxBleClient.observeStateChanges()
            .switchMap(state -> { // switchMap makes sure that if the state will change the rxBleClient.scanBleDevices() will dispose and thus end the scan
                switch (state) {

                    case READY:
                        // everything should work
                        return rxBleClient.scanBleDevices();
                    case BLUETOOTH_NOT_AVAILABLE:
                        // basically no functionality will work here
                    case LOCATION_PERMISSION_NOT_GRANTED:
                        // scanning and connecting will not work
                    case BLUETOOTH_NOT_ENABLED:
                        // scanning and connecting will not work
                    case LOCATION_SERVICES_NOT_ENABLED:
                        // scanning will not work
                    default:
                        return Observable.empty();
                }
            })
            .subscribe(
                    rxBleScanResult -> {
                        // Process scan result here.
                    },
                    throwable -> {
                        // Handle an error here.
                    }
            );

//
//// When done, just dispose.
//flowDisposable.dispose();


//    conection

    String macAddress = "AA:BB:CC:DD:EE:FF";
    RxBleDevice device = rxBleClient.getBleDevice(macAddress);

    Disposable disposable = device.establishConnection(autoConnect) // <-- autoConnect flag
            .subscribe(
                    rxBleConnection -> {
                        // All GATT operations are done through the rxBleConnection.
                    },
                    throwable -> {
                        // Handle an error here.
                    }
            );

//// When done... dispose and forget about connection teardown :)
//disposable.dispose();




    public void read(RxBleDevice device){

        System.out.println("=====> read: called");


        device.establishConnection(autoConnect)
                .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(characteristicUuid))
//                .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")))
                .subscribe(
                        characteristicValue -> {
                           System.out.println("=====> read characteristicValue:" + characteristicValue);
                        },
                        throwable -> {
                            System.out.println("=====> read throwable:" + throwable);
                        }
                );
    }

    public void notifications(){
        device.establishConnection(autoConnect)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUuid))
                .doOnNext(notificationObservable -> {
                    // Notification has been set up
                })
                .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                .subscribe(
                        bytes -> {
                            // Given characteristic has been changes, here is the value.
                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );
    }

    public void connectionState(){
        device.observeConnectionStateChanges()
                .subscribe(
                        connectionState -> {
                            // Process your way.
                        },
                        throwable -> {
                            // Handle an error here.
                        }
                );
    }
}
