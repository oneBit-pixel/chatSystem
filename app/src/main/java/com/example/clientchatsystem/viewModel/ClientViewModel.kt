package com.example.clientchatsystem.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.UtilsFileProvider
import com.example.clientchatsystem.model.LocalType
import com.example.clientchatsystem.model.MessageModel
import com.google.protobuf.InvalidProtocolBufferException
import com.example.clientchatsystem.model.MessageType
import com.example.clientchatsystem.viewModel.ConnectStatus.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser


import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import tutorial.SendMessage
import tutorial.SendMessage.Foo
import java.util.concurrent.TimeUnit

enum class ConnectStatus{
    CONNECTING,//连接中
    CONNECT_SUCCESS,//连接成功
    CONNECT_FAILURE,//连接失败
    DISCONNECT//断开连接
}
class ClientViewModel : ViewModel() {

    private val _messageList = MutableLiveData<List<MessageModel>>()
    val messageList: LiveData<List<MessageModel>> = _messageList

    val connectStatus=MutableLiveData<ConnectStatus>()

    lateinit var webSocket: WebSocket

    companion object {
        const val port = 8080
    }

    val ipAddress = NetworkUtils.getIPAddress(true)

    var myName: String = ""


    //连接
    fun connect(ip: String) {
        println("启动连接...")
        connectStatus.postValue(CONNECTING)
        val client = OkHttpClient.Builder()
            .pingInterval(30,TimeUnit.SECONDS)
            .build()
        val request = Request.Builder().url("ws://${ip}:${port}").build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                println("已关闭")
                connectStatus.postValue(DISCONNECT)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                println("正在关闭")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                println("连接失败${t.toString()}")
                connectStatus.postValue(CONNECT_FAILURE)
            }

            //处理收到的字符流
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)

                //解析Json字符串
//                val message = JsonParser().parse(text).asJsonObject
//                when (message["type"].asString) {
//                    "text" -> {
//                        val data = message["data"].asString
//                        val model = MessageModel(MessageType.TEXT, data)
//                        _messageList.postValue(_messageList.value.orEmpty() + model)
//                    }
//                    "image" -> {
//                        val data = message["data"].asString
//                        val model = MessageModel(MessageType.IMAGE, data)
//                        _messageList.postValue(_messageList.value.orEmpty() + model)
//                    }
//                }
            }

            //处理收到的字节流
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
//                //类型
//                val messageType = bytes[0]
//                //内容
//                val messageData = bytes.substring(1)
//                when (messageType.toInt()) {
//                    MessageType.TEXT.ordinal -> {
//                        println("收到文字消息")
//                        val textData = messageData.utf8()
//                        val message = MessageModel(MessageType.TEXT, textData)
//                        _messageList.postValue(messageList.value.orEmpty() + message)
//                    }
//                    MessageType.IMAGE.ordinal -> {
//                        println("收到图片消息")
//                        val imageData = messageData.base64()
//                        val message = MessageModel(MessageType.IMAGE, imageData)
//                        _messageList.postValue(messageList.value.orEmpty() + message)
//                    }
//                }

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

                        }
                    }
                }
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                println("连接成功")
                connectStatus.postValue(CONNECT_SUCCESS)
            }
        })
    }

    //发送信息
    fun sendMessage(str: String) {
        // 将图片数据打包成消息对象
//        val byteString = ByteArray(1) {
//            MessageType.TEXT.ordinal.toByte()
//        }.plus(str.toByteArray()).toByteString()
//        webSocket.send(byteString)


        //protocol buffer
        val message = Foo.newBuilder().apply {
            ip = ipAddress
            name = myName
            type = SendMessage.MessageType.TEXT
            data = com.google.protobuf.ByteString.copyFrom(str.toByteArray())
        }.build()

        val messageByte = message.toByteArray().toByteString()
        webSocket.send(messageByte)

    }

    fun disConnect() {
        webSocket.cancel()
    }

    //使用webSocket发送图片
    fun sendImage(byteArray: ByteArray) {
        //自拟协议
//        val typeByte = MessageType.IMAGE.ordinal.toByte()
//
//        val byteString = ByteArray(1) {
//            typeByte
//        }.plus(byteArray).toByteString()
//        println("字节大小==>${byteString.size}")
//        webSocket.send(byteString)

        //protocol buffer
        val message = Foo.newBuilder().apply {
            ip = ipAddress
            name = myName
            type = SendMessage.MessageType.IMAGE
            data = com.google.protobuf.ByteString.copyFrom(byteArray)
        }.build()


        val messageByte = message.toByteArray().toByteString()
        webSocket.send(messageByte)

    }

    fun setName(name: String?) {
        myName = if (name != null) {
            name
        } else {
            ""
        }
    }


}