package com.example.clientchatsystem.broadcast

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpBroadcaster(private val port:Int,private val message:String) {
    private val udpSocket=DatagramSocket(port)

    fun broadcast(){
        val packet = DatagramPacket(
            message.toByteArray(),
            message.length,
            //无限广播地址
            InetAddress.getByName("255.255.255.255"),
            port
        )
        udpSocket.broadcast = true
        udpSocket.send(packet)
        udpSocket.close()
    }
}