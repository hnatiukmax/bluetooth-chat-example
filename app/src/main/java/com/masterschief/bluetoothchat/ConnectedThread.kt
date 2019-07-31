package com.masterschief.bluetoothchat

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.provider.Settings
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets


class ConnectedThread(private val bSocket : BluetoothSocket, private val handler : Handler) : Thread() {

    private val bInStream : InputStream = bSocket.inputStream
    private val bOutStream : OutputStream = bSocket.outputStream
    private val bBuffer : ByteArray = ByteArray(1024)

    @ExperimentalStdlibApi
    override fun run() {
        write("Hello, World!".encodeToByteArray())
        StandardCharsets.UTF_8.encode("Hello, World")

        var numBytes : Int

        while (true) {
            numBytes = bInStream.read(bBuffer)
            val str = String(bBuffer.sliceArray(0 until numBytes))
            log("b2test ${str}")
            val msg = handler.obtainMessage(MESSAGE, str)
            handler.sendMessage(msg)
        }

    }

    fun write(bytes : ByteArray) {
        try {

            bOutStream.write(bytes)

        } catch (ex : IOException) {
            log("b2test Couldn't send data to the other device")
        }
    }

    fun cancel() {
        bSocket.close()
    }
}
