package com.example.chatsystem.viewModel.server

import okhttp3.Address
import okhttp3.OkHttpClient
import okhttp3.Request
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.HashSet

class ChatServer(address: InetSocketAddress) : WebSocketServer(address) {

    private lateinit var listener:WebSocketServerListener
    private val webSocketSet = Collections.synchronizedSet(LinkedHashSet<WebSocket>())
    companion object  {
        private const val port = 8080
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
        println("有客户端连接")
        //加入到集合中
        webSocketSet.add(conn)
        listener?.let {
            listener.onStatusChanged(WebSocketStatus.CONNECTED)
        }
    }

    //服务器关闭
    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        println("有客户端推出")
        listener?.let {
            listener.onStatusChanged(WebSocketStatus.DISCONNECTED)
        }
        webSocketSet.remove(conn)
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
        super.onMessage(conn, message)
        println("收到字节流信息...")
        sInstance.broadcast(message)
    }

    //收到信息
    override fun onMessage(conn: WebSocket?, message: String?) {
        println("收到消息了==>$message")
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
        println("服务端错误...")
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
