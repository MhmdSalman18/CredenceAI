package com.credenceai.app.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ExportUtils {
    fun exportAndShareReport(context: Context, csvData: String) {
        try {
            val directory = File(context.cacheDir, "reports")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val file = File(directory, "Credence_Report_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { out ->
                out.write(csvData.toByteArray())
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Credence Financial Report")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Export Report"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
