package com.masterschief.bluetoothchat

import android.bluetooth.BluetoothSocket

interface BluAct {
    fun showMessage(message : String)

    fun showBar()

    fun unshowBar()

    fun post(socket : BluetoothSocket?)
}