package com.example.clientchatsystem.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ToastUtils
import com.example.clientchatsystem.MainActivity
import com.example.clientchatsystem.ui.base.BaseVMActivity
import com.example.clientchatsystem.viewModel.ChooseViewModel
import com.example.clientchatsystem.viewModel.SearchState

/**
 * 使用多种不同的方式搜索 局域网内IP地址
 *
 * @param null
 * @return
 * @author zhangxuyang
 * @create 2023/4/12
 **/

class ChooseIPActivity : BaseVMActivity<ChooseViewModel>() {
    override fun getVMClass(): Class<ChooseViewModel> = ChooseViewModel::class.java

    @Composable
    override fun setComposeContent() {
        //通过Udp响应获取 局域网服务ip
        viewModel.searchServerByUdp()
        //查找状态
        val searchState by viewModel.searchState.observeAsState()
        Scaffold {
            it
//            Column {
            var text by remember { mutableStateOf("") }
            LazyColumn(verticalArrangement = when (searchState) {
                SearchState.SUCCESS -> {
                    Arrangement.Top
                }
                else -> {
                    Arrangement.Center
                }
            }, modifier = Modifier.fillMaxSize(), content = {
                when (searchState) {
                    SearchState.SEARCHING -> {
                        println("加载中...")
                        item {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                    SearchState.SUCCESS -> {
                        println("找到了...")
                        viewModel.serverIps.value?.let { ips ->
                            ToastUtils.showShort("查找到${ips.size}个服务端")
                        }
                        item {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                label = {
                                    Text(text = "请输入你的名字")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            )
                        }
                        items(viewModel.serverIps.value!!) { item ->
                            TextButton(modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    if (text.isEmpty()) {
                                        ToastUtils.showShort("名字不能为空")
                                    } else {
                                        println("点击Ip地址为==>$item")
                                        val bundle = Bundle()
                                        bundle.putString("serverIp", item)
                                        bundle.putString("myName", text)
                                        val intent =
                                            Intent(
                                                this@ChooseIPActivity,
                                                MainActivity::class.java
                                            )
                                        intent.putExtras(bundle)
                                        startActivity(intent)
                                    }
                                }) {
                                Text(text = item)
                            }
                        }
                    }
                    SearchState.FAILURE -> {
                        item {
                            TextButton(onClick = { viewModel.searchServerByUdp() }) {
                                Text(text = "查找失败点击重试")
                            }
                        }
                    }
                    SearchState.EMPTY -> {
                        item {
                            TextButton(
                                onClick = { viewModel.searchServerByUdp() },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(text = "查找内容为空点击重试")
                            }
                        }
                    }
                    null -> {
                        println("为空...")
                    }
                }

            })
//            }

        }
    }
}