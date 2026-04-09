package com.credenceai.app.data.local.entity



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Double,

    val type: String, // debit / credit

    val merchant: String?,

    val dateTime: Long,

    val category: String? = null,

    val source: String, // SMS or MANUAL

    val note: String? = null,

    val paymentMode: String? = null, // UPI / CARD / CASH

    val referenceId: String? = null
)