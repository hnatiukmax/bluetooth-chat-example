package com.masterschief.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.ParcelUuid
import java.io.IOException
import java.util.*

class ConnectThread(private val device: BluetoothDevice,
                    private val handler : Handler) : Thread() {
    private lateinit var bSocket : BluetoothSocket

    init {
        var tmp : BluetoothSocket? = null

        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))
        } catch (ex : IOException) {
            ex.message?.let { log(it) }
        }
        bSocket = tmp!!
    }

    override fun run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()

        log("b2test connect")
        try {
            bSocket.connect()
        } catch (ex : IOException) {
            bSocket.close()
            return
        }

        if (bSocket.isConnected) {
            val msg = handler.obtainMessage(CONNECTED)
            handler.sendMessage(msg)
            val manager  = ConnectedThread(bSocket, handler)
            manager.start()
        }
    }

    fun cancel() {
        bSocket.close()
    }
}