package com.example.clientchatsystem.viewModel

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.NetworkUtils
import com.example.clientchatsystem.model.LocalType
import com.example.clientchatsystem.model.MessageModel
import com.example.clientchatsystem.model.MessageType
import com.example.clientchatsystem.viewModel.ConnectStatus.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import okhttp3.*
import okio.ByteString.Companion.toByteString
import okio.IOException
import tutorial.SendMessage
import tutorial.SendMessage.Foo
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.Socket
import java.nio.ByteBuffer

enum class ConnectStatus {
    CONNECTING,//连接中
    CONNECT_SUCCESS,//连接成功
    CONNECT_FAILURE,//连接失败
    DISCONNECT//断开连接
}

class ClientViewModel : ViewModel() {

    private val _messageList = MutableLiveData<List<MessageModel>>()
    val messageList: LiveData<List<MessageModel>> = _messageList

    val connectStatus = MutableLiveData<ConnectStatus>()

    lateinit var webSocket: WebSocket

    companion object {
        const val port = 8080
    }

    val ipAddress = NetworkUtils.getIPAddress(true)

    var myName: String = ""

    private var socket: Socket? = null
    private val byteSize = 1024 * 1024

    //连接
    //使用tcp连接
    fun connect(ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            connectStatus.postValue(CONNECTING)
            try {
                //连接服务端
                socket = Socket(ip, port)
                connectStatus.postValue(CONNECT_SUCCESS)
                receiveMessage()
            } catch (e: IOException) {
                println("tcp连接出错：${e.toString()}")
                connectStatus.postValue(CONNECT_FAILURE)
            }
        }

    }

    private fun receiveMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            //此任务为阻塞 所以要开启多线程
            socket?.let {
                val buf = ByteArray(byteSize)
                while (!it.isClosed) {
                    println("开始接收数据")
                    val inputStream = it.getInputStream()
                    println("test...")
                    val reader = inputStream

                    //最后转为字节组
                    println("收到数据了...")

                    val outputStream = ByteArrayOutputStream()
                    val readSize = reader.read(buf)
                    outputStream.write(buf, 0, readSize)

                    outputStream.flush()
//                    outputStream.close()

                    val bytes = outputStream
                    println("收到数据的大小==>${bytes.size()}")
                    val newFoo = Foo.parseFrom(bytes.toByteArray())
                    newFoo.apply {
                        when (type) {
                            SendMessage.MessageType.TEXT -> {
                                val byteArray = data.toByteArray()
                                println("本机IP==>$ipAddress")
                                println("发送IP==>${newFoo.ip}")
                                val message = MessageModel(
                                    MessageType.TEXT,
                                    byteArray,
                                    local = if (ipAddress == newFoo.ip) {
                                        println("123")
                                        LocalType.RIGHT
                                    } else {
                                        LocalType.LEFT
                                    }, name
                                )
                                _messageList.postValue(messageList.value.orEmpty() + message)
                            }
                            SendMessage.MessageType.IMAGE -> {
                                println("收到图片了...")
                                val byteArray = data.toByteArray()
                                val message = MessageModel(
                                    MessageType.IMAGE,
                                    byteArray,
                                    local = if (ipAddress == newFoo.ip) {
                                        LocalType.RIGHT
                                    } else {
                                        LocalType.LEFT
                                    }, name
                                )
                                _messageList.postValue(messageList.value.orEmpty() + message)
                            }
                            SendMessage.MessageType.UNRECOGNIZED -> {
                                println("收到的数据没有该类型")
                            }
                        }
                    }
                }
                println("客户端断开连接...")
            } ?: run {
                println("socket为空...")
            }

        }

    }


    //使用websocket连接
//    fun connect(ip: String) {
//        println("启动连接...")
//        connectStatus.postValue(CONNECTING)
//        val client = OkHttpClient.Builder()
//            .pingInterval(30,TimeUnit.SECONDS)
//            .build()
//        val request = Request.Builder().url("ws://${ip}:${port}").build()
//
//        webSocket = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                super.onClosed(webSocket, code, reason)
//                println("已关闭")
//                connectStatus.postValue(DISCONNECT)
//            }
//
//            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
//                super.onClosing(webSocket, code, reason)
//                println("正在关闭")
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                super.onFailure(webSocket, t, response)
//                println("连接失败${t.toString()}")
//                connectStatus.postValue(CONNECT_FAILURE)
//            }
//
//            //处理收到的字符流
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                super.onMessage(webSocket, text)
//
//                //解析Json字符串
////                val message = JsonParser().parse(text).asJsonObject
////                when (message["type"].asString) {
////                    "text" -> {
////                        val data = message["data"].asString
////                        val model = MessageModel(MessageType.TEXT, data)
////                        _messageList.postValue(_messageList.value.orEmpty() + model)
////                    }
////                    "image" -> {
////                        val data = message["data"].asString
////                        val model = MessageModel(MessageType.IMAGE, data)
////                        _messageList.postValue(_messageList.value.orEmpty() + model)
////                    }
////                }
//            }
//
//            //处理收到的字节流
//            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//                super.onMessage(webSocket, bytes)
//                val newFoo = Foo.parseFrom(bytes.toByteArray())
//                newFoo.apply {
//                    when (type) {
//                        SendMessage.MessageType.TEXT -> {
//                            val byteArray = data.toByteArray()
//                            println("本机IP==>$ipAddress")
//                            println("发送IP==>${newFoo.ip}")
//                            val message = MessageModel(
//                                MessageType.TEXT,
//                                byteArray,
//                                local = if (ipAddress == newFoo.ip) {
//                                    println("123")
//                                    LocalType.RIGHT
//                                } else {
//                                    LocalType.LEFT
//                                }, name
//                            )
//                            _messageList.postValue(messageList.value.orEmpty() + message)
//                        }
//                        SendMessage.MessageType.IMAGE -> {
//                            val byteArray = data.toByteArray()
//                            val message = MessageModel(
//                                MessageType.IMAGE,
//                                byteArray,
//                                local = if (ipAddress == newFoo.ip) {
//                                    LocalType.RIGHT
//                                } else {
//                                    LocalType.LEFT
//                                }, name
//                            )
//                            _messageList.postValue(messageList.value.orEmpty() + message)
//                        }
//                        SendMessage.MessageType.UNRECOGNIZED -> {
//
//                        }
//                    }
//                }
//            }
//
//            override fun onOpen(webSocket: WebSocket, response: Response) {
//                super.onOpen(webSocket, response)
//                println("连接成功")
//                connectStatus.postValue(CONNECT_SUCCESS)
//            }
//        })
//    }

    //发送信息
    fun sendMessage(str: String) {
        viewModelScope.launch(Dispatchers.IO) {

            //protocol buffer
            val message = Foo.newBuilder().apply {
                ip = ipAddress
                name = myName
                type = SendMessage.MessageType.TEXT
                data = com.google.protobuf.ByteString.copyFrom(str.toByteArray())
            }.build().toByteArray()

            val messageByte = message.toByteString()
//        webSocket.send(messageByte)
            sendMessageByTcp(message)
        }

    }

    //tcp传输数据
    private fun sendMessageByTcp(message: ByteArray) {
        try {
            //阻塞任务
            socket?.let {
                println("开始发送信息")
                try {
                    val outputStream = it.getOutputStream()

                    //计算消息实际长度的字节组
                    val lengthBytes = ByteBuffer.allocate(4).putInt(message.size).array()
                    


                    val totalSize = message.size
                    println("message大小==>${totalSize}")
                    var offSet = 0 //起始字节
                    var length = byteSize //本次要传输大小 1MB

                    while (offSet < totalSize) {
                        //当起始字节 大于等于 总字节 代表发送完毕
                        if (offSet + byteSize >= totalSize) {
                            //代表一次传完
                            length = totalSize - offSet
                        }
                        outputStream.write(message, offSet, length)
                        offSet += length
                    }

                    //关闭流
                    outputStream.flush()
                    println("传完后字节大小==>$offSet")
                    if (it.isClosed) {
                        println("连接已断开")
                    } else {
                        println("仍然连接中...")
                    }
                } catch (e: Exception) {
                    println("发送消息出错了==>${e.toString()}")
                } finally {
                    println("当前线程==>${Thread.currentThread().name}")
                    println("主线程==>${Looper.getMainLooper().thread.name}")
                }
            }
        } catch (e: Exception) {
            println("传数据出错==>${e.toString()}")
        }


    }

    fun disConnect() {
//        webSocket.cancel()
        socket?.let {
            it.close()
        }
    }

    //使用webSocket发送图片
    fun sendImage(byteArray: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            //protocol buffer
            val message = Foo.newBuilder().apply {
                ip = ipAddress
                name = myName
                type = SendMessage.MessageType.IMAGE
                data = com.google.protobuf.ByteString.copyFrom(byteArray)
            }.build().toByteArray()


            val messageByte = message.toByteString()
            //        webSocket.send(messageByte)
            sendMessageByTcp(message)

        }

    }

    fun setName(name: String?) {
        myName = if (name != null) {
            name
        } else {
            ""
        }
    }


}