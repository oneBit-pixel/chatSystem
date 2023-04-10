package com.example.chatsystem.viewModel.server

import okhttp3.Address
import okhttp3.OkHttpClient
import okhttp3.Request
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class ChatServer(address: InetSocketAddress) : WebSocketServer(address) {

    private lateinit var listener:WebSocketServerListener

    companion object  {
        private const val port = 6666
        val sInstance by lazy(LazyThreadSafetyMode.NONE) {
            ChatServer(InetSocketAddress(port))
        }

    }


    //发送所有信息给组员
    private fun sendMessage(str: String) {
        sInstance.broadcast(str)
    }

    //成功建立连接
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        println("连接成功")
        listener?.let {
            listener.onStatusChanged(WebSocketStatus.CONNECTED)
        }
    }

    //服务器关闭
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        listener?.let {
            listener.onStatusChanged(WebSocketStatus.DISCONNECTED)
        }
    }

    //收到信息
    override fun onMessage(conn: WebSocket?, message: String?) {
        //当收到消息的时候 发送给客户端
        message?.let {
            sendMessage(it)
        }
        listener?.let {
            listener.onStatusChanged(WebSocketStatus.MESSAGE_RECEIVED)
        }
    }

    //连接发生异常
    override fun onError(conn: WebSocket?, ex: Exception?) {
        listener?.let {
            listener.onStatusChanged(WebSocketStatus.ERROR)
        }
    }

    //正在启动
    override fun onStart() {

    }

    fun addListener(listener: WebSocketServerListener){
        this.listener=listener
    }



    interface WebSocketServerListener {
        fun onStatusChanged(status: WebSocketStatus)
    }

}
enum class WebSocketStatus {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR,
    MESSAGE_RECEIVED
}
