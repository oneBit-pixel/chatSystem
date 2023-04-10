package com.example.chatsystem.viewModel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatsystem.viewModel.server.ChatServer
import com.example.chatsystem.viewModel.server.WebSocketStatus
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class ServerViewModel :ViewModel(), ChatServer.WebSocketServerListener {

    val webState=MutableLiveData<WebSocketStatus>()
    init {
        //启动webSocket服务端
        ChatServer.sInstance.addListener(this)
    }
    companion object{

    }

    fun startServer(port:Int){

    }

    override fun onStatusChanged(status: WebSocketStatus) {
        webState.postValue(status)
    }


}