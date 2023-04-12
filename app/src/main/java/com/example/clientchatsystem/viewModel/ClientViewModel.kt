package com.example.clientchatsystem.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clientchatsystem.model.MessageModel
import com.example.clientchatsystem.model.MessageType
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString


class ClientViewModel : ViewModel() {

    val webStats = MutableLiveData<WebSocketStatus>()

    private val _messageList = MutableLiveData<List<MessageModel>>()

    val messageList: LiveData<List<MessageModel>> = _messageList


    lateinit var webSocket: WebSocket

    companion object {
        const val port = 8080
    }


    enum class WebSocketStatus {
        ENTER_SUCCESS,//连接成功
        ENTER_FAILED,//连接失败
        CLOSED//退出成功
    }


    //连接
    fun connect(ip: String) {
        println("启动连接...")
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url("ws://${ip}:${port}").build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                println("已关闭")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                println("正在关闭")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                println("连接失败${t.toString()}")
            }

            //处理收到的字符流
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)

                //解析Json字符串
                val message = JsonParser().parse(text).asJsonObject
                when (message["type"].asString) {
                    "text" -> {
                        val data = message["data"].asString
                        val model = MessageModel(MessageType.TEXT, data)
                        _messageList.postValue(_messageList.value.orEmpty() + model)
                    }
                    "image" -> {
                        val data = message["data"].asString
                        val model = MessageModel(MessageType.IMAGE, data)
                        _messageList.postValue(_messageList.value.orEmpty() + model)
                    }
                }
            }

            //处理收到的字节流
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                //类型
                val messageType = bytes[0]
                //内容
                val messageData = bytes.substring(1)
                when (messageType.toInt()) {
                    MessageType.TEXT.ordinal -> {
                        println("收到文字消息")
                        val textData = messageData.utf8()
                        val message = MessageModel(MessageType.TEXT, textData)
                        _messageList.postValue(messageList.value.orEmpty() + message)
                    }
                    MessageType.IMAGE.ordinal -> {
                        println("收到图片消息")
                        val imageData = messageData.base64()
                        val message = MessageModel(MessageType.IMAGE, imageData)
                        _messageList.postValue(messageList.value.orEmpty() + message)
                    }
                }
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                println("连接成功")
            }
        })
    }

    //发送信息
    fun sendMessage(str: String) {
        // 将图片数据打包成消息对象
        val byteString = ByteArray(1) {
            MessageType.TEXT.ordinal.toByte()
        }.plus(str.toByteArray()).toByteString()
        webSocket.send(byteString)
    }

    fun disConnect() {
        webSocket.cancel()
    }

    //使用webSocket发送图片
    fun sendImage(byteArray: ByteArray) {
        val typeByte = MessageType.IMAGE.ordinal.toByte()
        val byteString = ByteArray(1) {
            typeByte
        }.plus(byteArray).toByteString()

        webSocket.send(byteString)
    }




}