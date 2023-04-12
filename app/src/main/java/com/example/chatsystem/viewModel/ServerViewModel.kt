package com.example.chatsystem.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatsystem.viewModel.server.ChatServer
import com.example.chatsystem.viewModel.server.WebSocketStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class ServerViewModel :ViewModel(), ChatServer.WebSocketServerListener {

    val webState=MutableLiveData<WebSocketStatus>()

    val udpPort=6666
    init {
        //启动webSocket服务端
        println("启动webSocket服务端")
        startServer()
    }
    private fun startServer(){
        ChatServer.sInstance.addListener(this)
        ChatServer.sInstance.start()
    }

     fun startUdpListener(){
         viewModelScope.launch(Dispatchers.IO){
             val socket = DatagramSocket(udpPort)
             while (true){
                 val receiveData = ByteArray(1024)
                 val receivePacket = DatagramPacket(receiveData, receiveData.size)
                 socket.receive(receivePacket)

                 val message = String(receivePacket.data, 0, receivePacket.length)
                 if (message=="zxy") {
                     println("收到客户端请求...")
                     //返回Ip地址
                     val sendData = "666".toByteArray()
                     val sendPacket = DatagramPacket(
                         sendData,
                         sendData.size,
                         receivePacket.address,
                         receivePacket.port
                     )
                     socket.broadcast=true
                     socket.send(sendPacket)
                 }
             }
         }

     }

    //断开连接
    fun disConnect(){
        ChatServer.sInstance.stop()
    }
    override fun onStatusChanged(status: WebSocketStatus) {
        webState.postValue(status)
    }

}