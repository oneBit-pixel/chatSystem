package com.example.clientchatsystem.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clientchatsystem.broadcast.UdpBroadcaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

class ChooseViewModel : ViewModel() {
    private val port = 6666
    private val message = "zxy"

    val serverIps: MutableLiveData<List<String>> = MutableLiveData()
    fun searchServerByUdp() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("Udp方式查找服务端")
                val udpBroadcaster = UdpBroadcaster(port, message)
                udpBroadcaster.broadcast()

                val servers = mutableListOf<String>()
                val buffer = ByteArray(1024)
                val datagramPacket = DatagramPacket(buffer, buffer.size)
                val datagramSocket = DatagramSocket(port)
                //超时
                datagramSocket.soTimeout = 3000

                while (true) {
                    try {
                        datagramSocket.receive(datagramPacket)
                        val receiveMsg = String(buffer, 0, datagramPacket.length)
                        println("receiveMsg==>$receiveMsg")
                        if (receiveMsg == "666") {
                            println("收到服务端IP")
                            servers.add(datagramPacket.address.hostAddress)
                        }
                    } catch (e: Exception) {
                        if (e is SocketTimeoutException) {
                            break
                        }else {
                            throw e
                        }
                    }
                }
                datagramSocket.close()
                serverIps.postValue(servers)
            }catch (e:Exception){
                println(e)
            }

        }
    }
}
