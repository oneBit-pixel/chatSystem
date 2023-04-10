package com.example.chatsystem.viewModel

import androidx.lifecycle.ViewModel
import com.example.chatsystem.viewModel.server.ChatServer

class ServerViewModel :ViewModel(){
    init {
        //启动webSocket服务端
        ChatServer
    }
}