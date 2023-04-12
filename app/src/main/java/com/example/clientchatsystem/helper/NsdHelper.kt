package com.example.clientchatsystem.helper

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo

class NsdHelper(context: Context) {

    private var discoveryListener: NsdManager.DiscoveryListener?=null
    private lateinit var serviceType: String
    private var serviceName: String? = null
    private val nsdManager:NsdManager=context.getSystemService(Context.NSD_SERVICE) as NsdManager

    fun discoverServices(serviceType: String, serviceName: String? = null) {
        this.serviceType = serviceType
        this.serviceName = serviceName

        stopDiscovery()

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
            }

            override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
            }

            override fun onDiscoveryStarted(p0: String?) {
            }

            override fun onDiscoveryStopped(p0: String?) {
            }

            override fun onServiceFound(p0: NsdServiceInfo?) {
            }

            override fun onServiceLost(p0: NsdServiceInfo?) {
            }

        }
    }

    private fun stopDiscovery() {
        discoveryListener?.let {
            nsdManager.stopServiceDiscovery(it)
        }
        discoveryListener=null
    }
}