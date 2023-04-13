package com.example.clientchatsystem.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clientchatsystem.broadcast.UdpBroadcaster
import com.example.clientchatsystem.viewModel.SearchState.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

enum class SearchState {
    SEARCHING,//查找中
    SUCCESS,//找到了
    FAILURE,//查找失败
    EMPTY
}

class ChooseViewModel : ViewModel() {
    private val port = 6666
    private val message = "zxy"

    val serverIps: MutableLiveData<List<String>> = MutableLiveData(emptyList())

    //查找状态
    val searchState = MutableLiveData<SearchState>()
    fun searchServerByUdp() {
        viewModelScope.launch(Dispatchers.IO) {
            searchState.postValue(SEARCHING)
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
                        //查找失败
                        searchState.postValue(FAILURE)
                        if (e is SocketTimeoutException) {
                            break
                        } else {
                            throw e
                        }
                    }
                }
                datagramSocket.close()
                serverIps.postValue(servers)
                println("servers==>${servers.toString()}")
                println("serverIps==>${serverIps.toString()}")
                if (servers.isNotEmpty()) {
                    searchState.postValue(SUCCESS)
                }else{
                    searchState.postValue(EMPTY)
                }
            } catch (e: Exception) {
                println(e)
                searchState.postValue(FAILURE)
            }

        }
    }
}
