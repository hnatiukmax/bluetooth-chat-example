package com.masterschief.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.ParcelUuid
import java.io.IOException
import android.hardware.usb.UsbDevice.getDeviceId
import android.content.Context.TELEPHONY_SERVICE
import android.os.Handler
import android.support.v4.content.ContextCompat.getSystemService
import android.telephony.TelephonyManager
import java.util.*


//as a server
class AcceptThread(private val handler : Handler) : Thread() {
    private lateinit var bServerSocket: BluetoothServerSocket


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

    override fun run() {
        var bSocket: BluetoothSocket? = null
        log("b2tst accept")

        val msg = handler.obtainMessage(RUN_SERVER)
        handler.sendMessage(msg)
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
                break
            }

        }

    }

    fun cancel() {
        try {
            bServerSocket.close()
        } catch (ex: IOException) {
            ex.message?.let { log(it) }
        }
    }


}