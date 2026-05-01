package com.credenceai.app.domain.usecase

import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(): String {
        val transactions = repository.getAllTransactions().first()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        val csvHeader = "ID,Date,Amount,Type,Category,Merchant,Source,Note,Payment Mode,Reference ID\n"
        val csvRows = transactions.joinToString("\n") { txn ->
            val date = dateFormat.format(Date(txn.dateTime))
            val escapedMerchant = txn.merchant?.replace("\"", "\"\"") ?: ""
            val escapedNote = txn.note?.replace("\"", "\"\"") ?: ""
            val escapedCategory = txn.category ?: "Uncategorized"
            
            "${txn.id},\"$date\",${txn.amount},${txn.type},\"$escapedCategory\",\"$escapedMerchant\",\"${txn.source}\",\"$escapedNote\",\"${txn.paymentMode ?: ""}\",\"${txn.referenceId ?: ""}\""
        }
        
        return csvHeader + csvRows
    }
}
