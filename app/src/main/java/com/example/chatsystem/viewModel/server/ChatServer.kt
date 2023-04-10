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

    companion object {
        private const val port = 6666
        val sInstance by lazy(LazyThreadSafetyMode.NONE) {
            ChatServer(InetSocketAddress(port))
        }
    }


    private fun startConnect() {
        sInstance.start()
    }

    //发送所有信息给组员
    private fun sendMessage(str:String){
        sInstance.broadcast(str)
    }

    //成功建立连接
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {

    }

    //服务器关闭
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {

    }

    //收到信息
    override fun onMessage(conn: WebSocket?, message: String?) {
        //当收到消息的时候 发送给客户端
        message?.let {
            sendMessage(it)
        }
    }

    //连接发生异常
    override fun onError(conn: WebSocket?, ex: Exception?) {

    }

    //正在启动
    override fun onStart() {

    }

}