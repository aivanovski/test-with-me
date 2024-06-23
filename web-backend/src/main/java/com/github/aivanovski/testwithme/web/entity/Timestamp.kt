package com.github.aivanovski.testwithme.web.entity

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Timestamp(
    val milliseconds: Long
) {

    fun formatForTransport(): String {
        return TRANSPORT_FORMAT.format(Date(milliseconds))
    }

    override fun toString(): String {
        return ISO_FORMAT.format(Date(milliseconds))
    }

    companion object {
        private val ISO_FORMAT = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            Locale.ENGLISH
        )

        private val TRANSPORT_FORMAT = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.ENGLISH
        )

        fun fromString(text: String): Timestamp {
            val time = ISO_FORMAT.parse(text)?.time ?: throw IllegalArgumentException()
            return Timestamp(time)
        }

        fun now(): Timestamp {
            return Timestamp(System.currentTimeMillis())
        }
    }
}