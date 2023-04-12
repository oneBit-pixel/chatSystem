package com.example.chatsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Observer
import com.example.chatsystem.base.BaseVMActivity
import com.example.chatsystem.viewModel.ServerViewModel
import com.example.chatsystem.viewModel.server.WebSocketStatus

class MainActivity : BaseVMActivity<ServerViewModel>() {

    override fun getVMClass(): Class<ServerViewModel> = ServerViewModel::class.java

    @Composable
    override fun setComposeContent() {
        Box(modifier = Modifier.fillMaxSize()){
            Text(
                text = "服务端",
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

    }

    override fun initEvent() {
        super.initEvent()
        viewModel.startUdpListener()
        viewModel.webState.observe(this, Observer {
            when (it) {
                WebSocketStatus.CONNECTING -> {

                }
                WebSocketStatus.CONNECTED -> {

                }
                WebSocketStatus.DISCONNECTED -> {

                }
                WebSocketStatus.ERROR -> {

                }
                WebSocketStatus.MESSAGE_RECEIVED -> {

                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disConnect()
    }
}