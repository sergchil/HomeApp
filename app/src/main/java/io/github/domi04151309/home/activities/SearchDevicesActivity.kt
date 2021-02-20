package io.github.domi04151309.home.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.net.wifi.WifiManager
import android.util.Log
import com._8rine.upnpdiscovery.UPnPDevice
import com._8rine.upnpdiscovery.UPnPDiscovery
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import android.widget.AdapterView
import android.widget.TextView
import io.github.domi04151309.home.R
import io.github.domi04151309.home.data.DeviceItem
import io.github.domi04151309.home.data.ListViewItem
import io.github.domi04151309.home.helpers.Devices
import io.github.domi04151309.home.adapters.ListViewAdapter
import io.github.domi04151309.home.helpers.Theme

class SearchDevicesActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        listView = findViewById(R.id.listView)
        val devices = Devices(this)
        val addresses = mutableListOf<String>()
        addresses.addAll(devices.getAll().map { it.address })
        //Countdown
        val waitItem = ListViewItem(
                title = resources.getString(R.string.pref_add_wait),
                summary = resources.getQuantityString(R.plurals.pref_add_wait_summary, 20, 20),
                icon = R.drawable.ic_info
        )
        listView.adapter = ListViewAdapter(this, arrayListOf(waitItem))

        Thread {
            Thread.sleep(1000L)
            val firstChildSummary = listView.getChildAt(0).findViewById<TextView>(R.id.summary)
            var count: Int
            for (i in 1 until 20) {
                runOnUiThread {
                    count = 20 - i
                    firstChildSummary.text = resources.getQuantityString(R.plurals.pref_add_wait_summary, count, count)
                }
                Thread.sleep(1000L)
            }
        }.start()

        //Device variables
        val listItems: ArrayList<ListViewItem> = arrayListOf()

        Thread {
            //Get compatible devices
            UPnPDiscovery.discoveryDevices(this, object : UPnPDiscovery.OnDiscoveryListener {
                override fun onStart() {}
                override fun onFoundNewDevice(device: UPnPDevice) {
                    val friendlyName = device.friendlyName
                    //todo device host adres@ aranc https-a
                    // addresses i mejinner@ https ov
                    // dra hamar chen ashxatum
                    if (device.manufacturer.equals("woweffectarmenia") && !addresses.contains(device.hostAddress)) {
                        listItems += ListViewItem(
                                title = device.friendlyName,
                                summary = device.hostAddress,
                                hidden = "SimpleHome API#Raspberry Pi",
                                icon = R.drawable.ic_device_raspberry_pi
                        )
                        addresses += device.hostAddress
                    }
                }

                override fun onFinish(devices: HashSet<UPnPDevice>) {}
                override fun onError(e: Exception) {
                    Log.e("UPnPDiscovery", "Error: " + e.localizedMessage)
                }
            })
        }.start()

        //Display found devices
        Handler().postDelayed({
            listView.adapter = ListViewAdapter(this, listItems)
        }, 20000)

        //Handle clicks
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
            val hidden =  view.findViewById<TextView>(R.id.hidden).text.toString()
            if (hidden != "") {

                val newItem = DeviceItem(devices.generateNewId())
                newItem.name = view.findViewById<TextView>(R.id.title).text.toString()
                newItem.address = view.findViewById<TextView>(R.id.summary).text.toString()
                newItem.mode = hidden.substring(0 , hidden.indexOf("#"))
                newItem.iconName = hidden.substring(hidden.lastIndexOf("#") + 1)

                if (devices.addressExists(newItem.address)) {
                    AlertDialog.Builder(this)
                            .setTitle(R.string.pref_add_success)
                            .setMessage("Device is already added in you list ")
                            .setPositiveButton(android.R.string.ok) { _, _ -> }
                            .show()

                } else {
                    devices.addDevice(newItem)
                    AlertDialog.Builder(this)
                            .setTitle(R.string.pref_add_success)
                            .setMessage(R.string.pref_add_success_message)
                            .setPositiveButton(android.R.string.ok) { _, _ -> }
                            .show()
                }
            }
        }
    }

    private fun intToIp(address: Int): String {
        return (address and 0xFF).toString() + "." + (address shr 8 and 0xFF) + "." + (address shr 16 and 0xFF) + "." + (address shr 24 and 0xFF)
    }
}
