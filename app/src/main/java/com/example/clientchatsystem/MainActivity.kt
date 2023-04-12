package com.example.clientchatsystem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.example.clientchatsystem.model.MessageModel
import com.example.clientchatsystem.model.MessageType
import com.example.clientchatsystem.ui.base.BaseVMActivity
import com.example.clientchatsystem.viewModel.ClientViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.ByteString.Companion.readByteString
import java.io.ByteArrayOutputStream

/**
 * 聊天室主页
 *
 * @param null
 * @return
 * @author zhangxuyang
 * @create 2023/4/12
 **/
class MainActivity : BaseVMActivity<ClientViewModel>() {


    override fun getVMClass(): Class<ClientViewModel> = ClientViewModel::class.java

    override fun initEvent() {
        super.initEvent()
        val bundle = intent.extras
        bundle?.apply {
            val ip = getString("serverIp")
            if (ip != null) {
                viewModel.connect(ip)
            }
        }
        viewModel.webStats.observe(this, Observer {
            when (it) {
                ClientViewModel.WebSocketStatus.ENTER_SUCCESS -> {
                    ToastUtils.showShort("成功进入群组")
                }
                ClientViewModel.WebSocketStatus.ENTER_FAILED -> {
                    ToastUtils.showShort("进入群组失败")
                }
                ClientViewModel.WebSocketStatus.CLOSED -> {
                    ToastUtils.showShort("退出成功")
                }
            }
        })
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // 处理选择的图片
        uri?.let {
            viewModel.apply {
                viewModelScope.launch(Dispatchers.IO) {
                    val stream = contentResolver.openInputStream(uri)

                    var bitmap = BitmapFactory.decodeStream(stream)
                    stream?.close()

                    //压缩图片
                    //目标大小
                    val targetSize = 1024 * 1024
                    //原始大小
                    val originalSize = bitmap.byteCount
                    val options = BitmapFactory.Options()
                    //设置压缩图片比例
                    options.inSampleSize = calculateInSampleSize(originalSize, targetSize)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                    var byteArray = outputStream.toByteArray()

                    while (byteArray.size > targetSize) {
                        options.inSampleSize *= 2
                        println("压缩比例==>${options.inSampleSize}")
                        outputStream.reset()
                        val newStream = contentResolver.openInputStream(uri)
                        //重新计算图片的大小
                        bitmap = BitmapFactory.decodeStream(newStream,null,options)
                        newStream?.close()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        byteArray = outputStream.toByteArray()
                        println("转换后的大小==>${(byteArray.size/targetSize)}")
                    }
                    //关闭流
                    outputStream.close()

                    sendImage(byteArray)
                }
            }
        }
    }

    fun calculateInSampleSize(originalSize: Int, targetSize: Int): Int {
        var inSampleSize = 1
        var size = originalSize
        while (size > targetSize) {
            size /= 2
            inSampleSize *= 2
        }
        println("压缩比例为$inSampleSize")
        return inSampleSize
    }

    @Composable
    override fun setComposeContent() {
        Scaffold {
            it
            var text by remember {
                mutableStateOf("")
            }

            val messageState by viewModel.messageList.observeAsState()

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                LazyColumn(reverseLayout = true, modifier = Modifier.weight(5f), content = {
                    messageState?.let { message ->
                        items(message.reversed()) { item: MessageModel ->
                            when (item.type) {
                                MessageType.TEXT -> {
                                    println("显示文字")
                                    Text(text = item.data)
                                }
                                MessageType.IMAGE -> {
                                    println("展示图片")
                                    //再将图片编码 转为字节数组
                                    val imageData = Base64.decode(item.data, Base64.DEFAULT)
                                    //将字节数组解码为Bitmap对象
                                    val bitmap =
                                        BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

//                                    println("压缩前大小==>${imageData.size}")
//                                    println("压缩后大小==>${stream.size()}")
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(200.dp)
                                    )

                                }
                            }
                        }
                    }
                })
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            pickImage.launch("image/*")
                        }
                        .weight(1f)
                )
                Box(
                    modifier = Modifier.weight(2f)
                ) {
                    TextField(
                        value = text, onValueChange = {
                            text = it
                        },
                        label = {
                            if (text.isEmpty()) {
                                Text(text = "请输入文字")
                            }
                        },
                        modifier = Modifier
                            .border(width = 2.dp, color = Color.Gray)
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.sendMessage(text)
                            text = ""
                        },
                        content = {
                            Text(text = "点击发送")
                        },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //断开连接
        viewModel.disConnect()
    }
}