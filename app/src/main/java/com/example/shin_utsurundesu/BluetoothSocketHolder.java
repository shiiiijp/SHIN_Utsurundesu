package com.example.shin_utsurundesu;

import android.bluetooth.BluetoothSocket;

public class BluetoothSocketHolder {
    private static BluetoothSocket socket;

    public static synchronized BluetoothSocket getSocket() {
        return socket;
    }

    public static synchronized void setSocket(BluetoothSocket socket) {
        BluetoothSocketHolder.socket = socket;
    }
}
