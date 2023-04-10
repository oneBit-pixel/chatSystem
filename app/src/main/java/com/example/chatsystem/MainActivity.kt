package com.example.chatsystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.Observer
import com.example.chatsystem.base.BaseVMActivity
import com.example.chatsystem.viewModel.ServerViewModel
import com.example.chatsystem.viewModel.server.WebSocketStatus

class MainActivity : BaseVMActivity<ServerViewModel>() {

    override fun getVMClass(): Class<ServerViewModel> =ServerViewModel::class.java

    @Composable
    override fun setComposeContent() {

    }

    override fun initEvent() {
        super.initEvent()
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
}