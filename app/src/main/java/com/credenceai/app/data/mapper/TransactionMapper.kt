package com.credenceai.app.data.mapper

import com.credenceai.app.data.local.entity.TransactionEntity
import com.credenceai.app.domain.model.Transaction


fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        type = type,
        merchant = merchant,
        dateTime = dateTime,
        category = category,
        source = source,
        note = note,
        paymentMode = paymentMode,
        referenceId = referenceId
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        type = type,
        merchant = merchant,
        dateTime = dateTime,
        category = category,
        source = source,
        note = note,
        paymentMode = paymentMode,
        referenceId = referenceId
    )
}