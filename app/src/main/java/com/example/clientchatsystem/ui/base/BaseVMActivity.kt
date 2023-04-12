package com.example.clientchatsystem.ui.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseVMActivity<VM:ViewModel>: AppCompatActivity() {
    protected lateinit var viewModel:VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel=ViewModelProvider(this)[getVMClass()]
        initEvent()
        setContent {
            setComposeContent()
        }
    }

    open fun initEvent() {

    }

    abstract fun getVMClass(): Class<VM>

    @Composable
    abstract fun setComposeContent()
}