package com.freewill.phayathaidetect;

import android.bluetooth.BluetoothAdapter;
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GetUUID {
    public GetUUID() {

    }

    public void GetUUIDADAPTER(BluetoothAdapter adapter, String  deviceName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        adapter = BluetoothAdapter.getDefaultAdapter();

        Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

        ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);

        for (ParcelUuid uuid: uuids) {
            Log.d("UUIIDD", "device name: "+deviceName+ "UUID: " + uuid.getUuid().toString());
        }
    }

    public void getUUIDDEVICE(BluetoothAdapter adapter, String  deviceName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        adapter = BluetoothAdapter.getDefaultAdapter();

        Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

        ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);

        for (ParcelUuid uuid: uuids) {
            Log.d("UUIIDD", "device name: "+deviceName+ "UUID: " + uuid.getUuid().toString());
        }
    }
}
