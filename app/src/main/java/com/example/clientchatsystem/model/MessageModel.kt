package com.example.clientchatsystem.model


/**
 * 存放文字或者图片
 *
 * @return
 * @author zhangxuyang
 * @create 2023/4/11
 **/

data class MessageModel(
    val type:MessageType,
    val data:ByteArray,
    val local:LocalType=LocalType.LEFT,
    val name:String
)

enum class LocalType{
    LEFT,
    RIGHT
}
enum class MessageType {
    TEXT,
    IMAGE,
}