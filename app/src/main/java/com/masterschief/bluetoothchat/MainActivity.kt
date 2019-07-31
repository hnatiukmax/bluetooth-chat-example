package com.masterschief.bluetoothchat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Logger

class MainActivity : AppCompatActivity(), BluAct {
    private val log = Logger.getLogger(this::class.java!!.getName())

    private lateinit var filter : IntentFilter
    val REQUEST_ENABLE_BT = 1
    lateinit var bAdapter: BluetoothAdapter

    private lateinit var pairedDevices : Set<BluetoothDevice>
    private var discoveredDevices = mutableSetOf<BluetoothDevice>()

    private val devicesNames = ArrayList<String>()

    //UI
    lateinit var btn_onBluetooth: Button
    lateinit var btn_offBluetooth: Button
    lateinit var btn_bluetoothCheck: Button
    lateinit var btn_paired: Button
    lateinit var btn_discover: Button
    lateinit var btn_server: Button
    lateinit var btn_visible: Button
    lateinit var textView_typeDevices: TextView
    lateinit var listView_devices : ListView
    lateinit var bar : ProgressBar
    lateinit var btn_send : Button
    lateinit var textView_chat : TextView
    lateinit var editText_message : EditText

    private lateinit var manager : ConnectedThread

    //adapter
    private lateinit var listView_devices_adapter : ArrayAdapter<String>

    val handler = Handler {
        when(it.what) {
            CONNECTED -> {
                showMessage("Connected")
            }
            RUN_SERVER -> {
                showMessage("Run server")
            }
            MESSAGE -> {
                textView_chat.text = (it.obj as String)
            }
        }
        true
    }





    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()

        //Bluetooth
        bAdapter = BluetoothAdapter.getDefaultAdapter()

        filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    //UI
    @ExperimentalStdlibApi
    fun initUI() {
        bar = findViewById(R.id.progressBar)
        bar.visibility = View.INVISIBLE

        listView_devices_adapter  = ArrayAdapter(this, android.R.layout.simple_list_item_1, devicesNames)

        btn_onBluetooth = findViewById(R.id.btn_onBluetooth)
        btn_offBluetooth = findViewById(R.id.btn_offBluetooth)
        btn_bluetoothCheck = findViewById(R.id.btn_bluetoothCheck)
        btn_paired = findViewById(R.id.btn_paired)
        btn_discover = findViewById(R.id.btn_discover_devices)
        listView_devices = findViewById(R.id.listView_devices)
        textView_typeDevices = findViewById(R.id.textView_typeDevices)
        btn_server = findViewById(R.id.btn_server)
        btn_visible = findViewById(R.id.btn_visible)
        btn_send = findViewById(R.id.btn_send)
        textView_chat = findViewById(R.id.textView_chat)
        editText_message = findViewById(R.id.editText_field)


        btn_onBluetooth.setOnClickListener(btn_onBluetooth_listener)
        btn_offBluetooth.setOnClickListener(btn_offBluetooth_listener)
        btn_bluetoothCheck.setOnClickListener(btn_bluetoothCheck_listener)
        btn_paired.setOnClickListener(btn_paired_listener)
        btn_discover.setOnClickListener(btn_discover_listener)
        listView_devices.adapter = listView_devices_adapter
        listView_devices.setOnItemClickListener(listView_devices_listener)
        btn_server.setOnClickListener(btn_server_listener)
        btn_visible.setOnClickListener(btn_visible_listener)
        btn_send.setOnClickListener(btn_send_listener)
    }

    /*
        Listeners
    */

    //btn send
    @ExperimentalStdlibApi
    val btn_send_listener = View.OnClickListener {
        manager.write(editText_message.text.toString().encodeToByteArray())
    }

    //btn ON
    val btn_onBluetooth_listener = View.OnClickListener {
        if (bAdapter.isEnabled) {
            showMessage("Bluetooth already is On")
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    //btn OFF
    val btn_offBluetooth_listener = View.OnClickListener {
        if (bAdapter.isEnabled) {
           bAdapter.disable()
            showMessage("Bluetooth is OFF")
        } else {
            showMessage("Bluetooth is already OFF")
        }
    }

    //btn CHECK
    val btn_bluetoothCheck_listener = View.OnClickListener {
        showMessage("Bluetooth is ${if (bAdapter != null) "present" else "absent"}")
    }

    //btn PAIRED
    val btn_paired_listener = View.OnClickListener {
        pairedDevices = bAdapter.bondedDevices

        devicesNames.clear()
        listView_devices_adapter.notifyDataSetChanged()

        textView_typeDevices.text = "PAIRED DEVICES"
        for (device in pairedDevices) {
            devicesNames.add(device.name)
            showMessage(device.name)
            listView_devices_adapter.notifyDataSetChanged()
        }

    }

    //btn DISCOVER
    val btn_discover_listener = View.OnClickListener {
        textView_typeDevices.text = "DISCOVERED DEVICES"

        devicesNames.clear()
        listView_devices_adapter.notifyDataSetChanged()

        if (bAdapter.isEnabled) {
            if (bAdapter.isDiscovering) {
                bAdapter.cancelDiscovery()
                unregisterReceiver(receiver)
            }
            registerReceiver(receiver, filter)
            bAdapter.startDiscovery()
        } else {
            showMessage("Bluetooth is OFF")
        }
    }

    //listView listener
    val listView_devices_listener = AdapterView.OnItemClickListener {
            adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
        val connect = AsynkConnectThread(this)
        connect.execute(discoveredDevices.elementAt(i))

    }

    //btn SERVER listener
    val btn_server_listener = View.OnClickListener {
        val accept = AsynkAcceptThread(this)
        accept.execute()


        //val acceptThread = AcceptThread(handler)
        //acceptThread.start()
    }

    //btn VISIBLE
    val btn_visible_listener = View.OnClickListener {
        showMessage("visible")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)
    }

    override fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showBar() {
        bar.visibility = View.VISIBLE
    }

    override fun unshowBar() {
        bar.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showMessage(resultCode.toString())
        when(resultCode) {
            -1 -> {
                showMessage("Bluetooth is On")
            }
        }

    }

    //discover new devices
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            showMessage("kek")
            val action: String = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    discoveredDevices.add(device)
                    devicesNames.add(device.name)
                    log(device.name)
                    listView_devices_adapter.notifyDataSetChanged()
                }
            }
        }
    }

    @ExperimentalStdlibApi
    override fun post(socket : BluetoothSocket?) {
        socket?.let {
            manager = ConnectedThread(it, handler)
            manager.start()
            manager.write("yeap".encodeToByteArray())
        }
    }


}