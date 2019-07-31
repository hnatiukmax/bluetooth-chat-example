package com.masterschief.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Handler
import java.io.IOException
import java.util.*

class AsynkConnectThread(val act : BluAct) : AsyncTask<BluetoothDevice, Handler, BluetoothSocket>() {
    private lateinit var bSocket : BluetoothSocket

    override fun onPreExecute() {
        super.onPreExecute()
        act.showBar()
    }

    override fun doInBackground(vararg params: BluetoothDevice?): BluetoothSocket {
        bSocket = params[0]!!.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID))

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery()

        log("b2test connect")
        try {
            bSocket.connect()
        } catch (ex : IOException) {
            bSocket.close()
        }

        return bSocket
    }

    override fun onPostExecute(result: BluetoothSocket?) {
        super.onPostExecute(result)
        if (bSocket.isConnected) {
            act.unshowBar()
            act.post(result)
        }
    }

    fun cancel() {
        bSocket.close()
    }
}