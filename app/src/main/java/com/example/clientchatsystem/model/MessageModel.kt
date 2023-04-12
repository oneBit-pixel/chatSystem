package com.example.clientchatsystem.model

/**
 * 存放文字或者图片
 *
 * @param null
 * @return
 * @author zhangxuyang
 * @create 2023/4/11
 **/

data class MessageModel(
    val type:MessageType,
    val data:String
)
enum class MessageType {
    TEXT,
    IMAGE,
}