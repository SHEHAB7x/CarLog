package com.example.carlog.network

import com.example.carlog.obd.ObdCommand
import com.example.carlog.utils.RegexPatterns.BUS_INIT_PATTERN
import com.example.carlog.utils.RegexPatterns.COLON_PATTERN
import com.example.carlog.utils.RegexPatterns.WHITESPACE_PATTERN
import com.example.carlog.utils.removeAll

fun <T> T.pipe(vararg functions: (T) -> T): T =
    functions.fold(this) { value, f -> f(value) }


data class ObdRawResponse(
    val value: String,
    val elapsedTime: Long
) {
    private val valueProcessorPipeline by lazy {
        arrayOf<(String) -> String>(
            {
                removeAll(WHITESPACE_PATTERN, it) // removes all [ \t\n\x0B\f\r]
            },
            {
                removeAll(BUS_INIT_PATTERN, it)
            },
            {
                removeAll(COLON_PATTERN, it)
            }
        )
    }

    private val processedValue by lazy { value.pipe(*valueProcessorPipeline) }

    val bufferedValue by lazy {
        processedValue.chunked(2) { it.toString().toInt(radix = 16) }.toIntArray()
    }
}

data class ObdResponse(
    val command: ObdCommand,
    val rawResponse: ObdRawResponse,
    val value: String,
    val unit: String = ""
) {
    val formattedValue: String get() = command.format(this)
}