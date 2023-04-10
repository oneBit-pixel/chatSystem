package com.example.chatsystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.example.chatsystem.base.BaseVMActivity
import com.example.chatsystem.viewModel.ServerViewModel

class MainActivity : BaseVMActivity<ServerViewModel>() {

    override fun getVMClass(): Class<ServerViewModel> =ServerViewModel::class.java

    @Composable
    override fun setComposeContent() {

    }

    override fun initEvent() {
        super.initEvent()
        
    }
}