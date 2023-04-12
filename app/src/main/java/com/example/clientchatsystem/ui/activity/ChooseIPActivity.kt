package com.example.clientchatsystem.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clientchatsystem.MainActivity
import com.example.clientchatsystem.ui.base.BaseVMActivity
import com.example.clientchatsystem.viewModel.ChooseViewModel

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
        val chooseList = listOf(
            "Udp",
//            "mDNS",
//            "Wifi",
//            "WifiP2p"
        )
        val serverIpsStatus by viewModel.serverIps.observeAsState(emptyList())
        Scaffold {
            it
            Column {
                LazyRow(contentPadding = PaddingValues(5.dp), content = {
                    items(chooseList) { item ->
                        Button(onClick = {
                            println("点击了$item")
                            when (item) {
                                "Udp" -> {
                                    viewModel.searchServerByUdp()
                                }
                                else -> {}
                            }
                        }, modifier = Modifier.padding(5.dp)) {
                            Text(text = item)
                        }
                    }
                })
                LazyColumn(content = {
                    when (serverIpsStatus.isEmpty()) {
                        true -> {
                            item {
                                Text(text = "暂无数据..")
                            }
                        }
                        false -> {
                            items(serverIpsStatus) { item ->
                                TextButton(modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        println("点击Ip地址为==>$item")
                                        val bundle = Bundle()
                                        bundle.putString("serverIp",item)
                                        val intent = Intent(this@ChooseIPActivity,MainActivity::class.java)
                                        intent.putExtras(bundle)
                                        startActivity(intent)
                                    }) {
                                    Text(text = item)
                                }
                            }
                        }
                    }
                })
            }

        }
    }
}