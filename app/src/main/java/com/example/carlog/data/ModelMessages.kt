package com.example.carlog.data

data class ModelMessages(
    val getMessages: List<GetMessage>,
    val statuscode: StatusCode
)
data class Message(
    val body: String,
    val dateTime: String
)
data class GetMessage(
    val messages: List<Message>,
    val name: String
)