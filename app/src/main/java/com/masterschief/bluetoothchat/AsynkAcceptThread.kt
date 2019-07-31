package com.masterschief.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Handler
import java.io.IOException
import java.util.*

class AsynkAcceptThread(val act : BluAct) : AsyncTask<Void, Handler, BluetoothSocket?>() {
    private lateinit var bServerSocket: BluetoothServerSocket
    private var bSocket: BluetoothSocket? = null

    init {
        var tmp: BluetoothServerSocket? = null
        try {
            val uuid = UUID.randomUUID()
            tmp = BluetoothAdapter
                .getDefaultAdapter()
                .listenUsingRfcommWithServiceRecord("BluetoothChat", UUID.fromString(MY_UUID))
        } catch (ex: IOException) {
            ex.message?.let { log(it) }
        }
        bServerSocket = tmp!!

    }

    fun cancel() {
        try {
            bServerSocket.close()
        } catch (ex: IOException) {
            ex.message?.let { log(it) }
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        act.showBar()
    }

    override fun doInBackground(vararg params: Void?): BluetoothSocket? {
        var bSocket: BluetoothSocket? = null
        log("b2tst accept")

        //val msg = handler.obtainMessage(RUN_SERVER)
        //handler.sendMessage(msg)
        while (true) {
            try {
                bSocket = bServerSocket.accept()
            } catch (ex: IOException) {
                ex.message?.let { log(it) }
                break
            }

            if (bSocket != null) {
                log("b2test bSocket != null")
                //val manager  = ConnectedThread(bSocket)
                //manager.start()

                bServerSocket.close()
                return bSocket
                break
            }
        }
        return null
    }

    override fun onPostExecute(result: BluetoothSocket?) {
        super.onPostExecute(result)
        act.unshowBar()
        act.post(result)
    }


}

