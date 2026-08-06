package com.credenceai.app.domain.model

data class Transaction(

    val id: Int = 0,
    val amount: Double,
    val type: String,

    val merchant: String?,

    val dateTime: Long,

    val category: String?,

    val source: String,

    val note: String?,

    val paymentMode: String?,

    val referenceId: String?
)