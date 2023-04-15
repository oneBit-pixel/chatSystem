package com.example.clientchatsystem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.example.clientchatsystem.model.LocalType
import com.example.clientchatsystem.model.MessageModel
import com.example.clientchatsystem.model.MessageType
import com.example.clientchatsystem.ui.base.BaseVMActivity
import com.example.clientchatsystem.viewModel.ClientViewModel
import com.example.clientchatsystem.viewModel.ConnectStatus
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

    var ip:String=""
    override fun initEvent() {
        super.initEvent()
        val bundle = intent.extras
        bundle?.apply {
            ip = getString("serverIp").toString()
            if (ip != null) {
                viewModel.connect(ip)
            }
            val name = getString("myName")
            viewModel.setName(name)
        }
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
                        bitmap = BitmapFactory.decodeStream(newStream, null, options)
                        newStream?.close()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        byteArray = outputStream.toByteArray()
                        println("转换后的大小==>${(byteArray.size )}")
                    }
                    //关闭流
                    outputStream.close()

                    sendImage(byteArray)
                }
            }
        }
        println("成功转发图片")
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
            val connectState by viewModel.connectStatus.observeAsState()

            when (connectState) {
                ConnectStatus.CONNECTING -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    }
                }
                ConnectStatus.CONNECT_FAILURE -> {
                    TextButton(onClick = {
                        viewModel.connect(ip)
                    }, modifier = Modifier.fillMaxSize()) {
                        Text(text = "连接聊天室失败，点击重连")
                    }
                }
                ConnectStatus.DISCONNECT -> {
                    TextButton(onClick = {
                        viewModel.connect(ip)
                    }, modifier = Modifier.fillMaxSize()) {
                        Text(text = "聊天室连接已断开，点击重连")
                    }
                }
                ConnectStatus.CONNECT_SUCCESS -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        LazyColumn(reverseLayout = true,modifier = Modifier
                            .weight(5f)
                            .fillMaxWidth()
                            , content = {
                                messageState?.let { message ->
                                    items(message.reversed()) { item: MessageModel ->
                                        var align: Alignment.Horizontal? = null
                                        var color: Color? = null
                                        val shape = RoundedCornerShape(5.dp)
                                        when (item.local) {
                                            LocalType.LEFT -> {
                                                println("居左")
                                                align = Start
                                                color = Color.Gray
                                            }
                                            LocalType.RIGHT -> {
                                                println("居右")
                                                align = End
                                                color = Color.Green
                                            }
                                        }
                                        when (item.type) {
                                            MessageType.TEXT -> {
                                                println("显示文字")
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp)
                                                ) {
                                                    Text(
                                                        text = "${item.name}:",
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.align(align)
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(align!!)
                                                            .background(
                                                                color = color,
                                                                shape = shape
                                                            ),
                                                    ) {
                                                        Text(
                                                            text = String(item.data),
                                                            fontSize = 20.sp,
                                                            modifier = Modifier.padding(5.dp)
                                                        )
                                                    }

                                                }

                                            }
                                            MessageType.IMAGE -> {
                                                println("展示图片")
                                                //将字节数组解码为Bitmap对象
                                                val bitmap =
                                                    BitmapFactory.decodeByteArray(
                                                        item.data,
                                                        0,
                                                        item.data.size
                                                    )

                                                Column(
                                                    modifier = Modifier
                                                        .padding(10.dp)
                                                        .fillMaxWidth()

                                                ) {
                                                    Text(
                                                        text = "${item.name}:",
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.align(align)
                                                    )
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(200.dp)
                                                            .align(align!!)
                                                            .background(
                                                                color = color,
                                                                shape = shape
                                                            )
                                                    )
                                                }

                                            }
                                        }
                                    }
                                }
                            })
                        IconButton(
                            onClick = {
                                pickImage.launch("image/*")
                            }, modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                            )
                        }

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

                null -> {
                    println("空状态...")
                }
            }



        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //断开连接
        viewModel.disConnect()
    }

    @Preview
    @Composable
    fun PreviewImage() {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier
                .clickable {
                    pickImage.launch("image/*")
                }
        )
    }
}