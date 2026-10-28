package com.credenceai.credence_ai_flutter.notifications

import java.math.BigDecimal
import java.security.MessageDigest
import java.util.Locale

/** Pure Kotlin parsing policy, independent of Android and the Flutter engine. */
object TransactionParser {
    val financialPackages = setOf(
        "com.google.android.apps.nbu.paisa.user", "com.phonepe.app", "in.org.npci.upiapp",
        "com.amazon.mShop.android.shopping", "com.supermoney.app", "com.paytm.business",
        "net.one97.paytm", "com.mobikwik_new", "com.freecharge.android", "com.dreamplug.androidapp",
        "com.slice.mycard", "com.htc.wallet", "com.sbi.SBIFreedomPlus", "com.msf.kbank.mobile",
        "com.axis.mobile", "com.csam.icici.bank.imobile", "com.snapwork.hdfc", "com.indusind.mobile",
        "com.idbi.mpassbook", "com.Bank.BOI", "com.pnbmobilebanking", "com.google.android.apps.messaging",
        "com.samsung.android.messaging", "com.android.mms", "org.thoughtcrime.securesms"
    )
    private val smsPackages = setOf("com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms", "org.thoughtcrime.securesms")
    private val senders = setOf("HDFCBK", "SBIINB", "ICICIB", "KOTAKB", "AXISBK", "INDBNK", "PNBSMS", "BOIIND", "CENTBK", "CANBNK", "UNIONB", "SYNDBK", "IDBIBK", "FEDBAK", "YESBNK", "PAYTMB", "GPAY", "PHONEPE", "BHIMUPI", "CREDCL", "SCBNK", "HSBCIN", "CITIBK", "RBLBNK", "IDFCBK", "AUBANK", "LVBNK", "AMZPAY", "SLICEIT")
    private val noise = Regex("""\b(otp|password|login|verification|discount|offer)\b|one.time.password|sign.in""", RegexOption.IGNORE_CASE)
    private val amountPattern = Regex("""(₹|Rs\.?|INR|\$|USD|€|EUR|£|GBP)\s*([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
    private val credit = Regex("""credited|received|refund|cashback|deposited|paid you|sent you|transferred to you|added to""", RegexOption.IGNORE_CASE)
    private val debit = Regex("""debited|spent|withdrawn|payment of|purchase of|charged|deducted|you paid|you sent|paid to|transferred to""", RegexOption.IGNORE_CASE)
    private val upiMerchant = Regex("""(?:You paid|Paid to|Sent to)\s+(.*?)\s+(?:₹|Rs|INR|\$|€|£)|^(.*?)\s+(?:paid you|sent you)\s+(?:₹|Rs|INR|\$|€|£)""", RegexOption.IGNORE_CASE)
    private val merchantPattern = Regex("""\b(?:to|at|from)\s+([A-Za-z][A-Za-z0-9@._ -]{1,60})""", RegexOption.IGNORE_CASE)
    private val referencePattern = Regex("""(?:UPI\s*Ref(?:\s*No)?|UTR|Ref(?:erence)?(?:\s*(?:No|Number))?)[.:#\s-]*([A-Za-z0-9]{6,30})""", RegexOption.IGNORE_CASE)

    fun parse(packageName: String, title: String, text: String, timestamp: Long, notificationKey: String): Map<String, Any?>? {
        if (packageName !in financialPackages || timestamp <= 0) return null
        val sms = packageName in smsPackages
        val sender = title.trim().uppercase(Locale.ROOT).replace(Regex("^[A-Z]{2}-"), "").removeSuffix("-S").removeSuffix("-T")
        if (sms && sender !in senders) return null
        val full = "$title $text".trim()
        if (noise.containsMatchIn(full)) return null
        val match = amountPattern.find(full) ?: return null
        val minor = try { BigDecimal(match.groupValues[2].replace(",", "")).movePointRight(2).longValueExact() } catch (_: Exception) { return null }
        if (minor <= 0 || minor > 99999999999999L) return null
        val isCredit = credit.containsMatchIn(full)
        val isDebit = debit.containsMatchIn(full)
        if (!isCredit && !isDebit) return null
        val lower = full.lowercase(Locale.ROOT)
        val type = when {
            listOf("paid you", "sent you", "received from", "transferred to you").any(lower::contains) -> "credit"
            listOf("you paid", "you sent", "paid to").any(lower::contains) -> "debit"
            isCredit && !isDebit -> "credit"
            else -> "debit"
        }
        val symbol = match.groupValues[1].uppercase(Locale.ROOT)
        val currency = when (symbol) { "$", "USD" -> "USD"; "€", "EUR" -> "EUR"; "£", "GBP" -> "GBP"; else -> "INR" }
        val upi = upiMerchant.find(full)
        val specificMerchant = upi?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }
        val genericMerchant = merchantPattern.find(text)?.groupValues?.get(1)?.split(Regex("""\s+(?:on|with|ref|using|for)\b""", RegexOption.IGNORE_CASE))?.firstOrNull()?.trim()
        val merchant = (specificMerchant ?: genericMerchant ?: if (!sms) title.takeIf(String::isNotBlank) else null)?.trim()?.take(120) ?: "Bank transaction"
        val ref = referencePattern.find(full)?.groupValues?.get(1)
        val identity = if (ref != null) "$currency:$type:$minor:$ref" else "$packageName:$notificationKey:$timestamp:$full"
        val id = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray()).joinToString("") { "%02x".format(it) }
        val mode = when { packageName.contains("nbu.paisa") || packageName.contains("phonepe") || packageName.contains("upiapp") -> "UPI"; sms -> "SMS"; else -> "APP" }
        return mapOf("id" to id, "amount_minor" to minor, "type" to type, "merchant" to merchant,
            "date_ms" to timestamp, "currency" to currency, "category" to null,
            "source" to "NOTIFICATION", "note" to "Detected from $packageName",
            "payment_mode" to mode, "reference_id" to "notification:$id")
    }
}
