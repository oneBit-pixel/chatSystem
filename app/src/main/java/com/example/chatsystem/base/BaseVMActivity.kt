package com.example.chatsystem.base

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get


abstract class BaseVMActivity<VM:ViewModel> :ComponentActivity(){

    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel= ViewModelProvider(this)[getVMClass()]
        initEvent()
        setContent{
            setComposeContent()
        }
    }

    open fun initEvent(){}

    abstract fun getVMClass(): Class<VM>

    @Composable
    abstract fun setComposeContent()


}