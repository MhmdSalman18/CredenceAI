package com.credenceai.app.data.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var addTransactionUseCase: AddTransactionUseCase

    @Inject
    lateinit var preferencesManager: com.credenceai.app.core.preferences.PreferencesManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isTrackingEnabled = true
    private var preferenceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service Created")
        preferenceJob = serviceScope.launch {
            preferencesManager.isSmsTrackingEnabled.collect { enabled ->
                isTrackingEnabled = enabled
                Timber.d("Tracking enabled status: $enabled")
            }
        }
    }

    override fun onDestroy() {
        preferenceJob?.cancel()
        super.onDestroy()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.d("✅ Notification Listener Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.d("❌ Notification Listener Disconnected")
    }

    // ── Allowlist: only parse notifications from these packages ───────────────
    // Add or remove packages based on what's installed on your target devices.
    private val FINANCIAL_PACKAGES = setOf(
        // ── UPI / Payment apps ──
        "com.google.android.apps.nbu.paisa.user",   // Google Pay
        "com.phonepe.app",                           // PhonePe
        "in.org.npci.upiapp",                        // BHIM
        "com.amazon.mShop.android.shopping",         // Amazon Pay
        "com.supermoney.app",                        // SuperMoney
        "com.paytm.business",                        // Paytm for Business
        "net.one97.paytm",                           // Paytm
        "com.mobikwik_new",                          // MobiKwik
        "com.freecharge.android",                    // FreeCharge
        "com.dreamplug.androidapp",                  // CRED
        "com.slice.mycard",                          // Slice

        // ── Bank apps ──
        "com.htc.wallet",
        "com.sbi.SBIFreedomPlus",                   // SBI YONO
        "com.msf.kbank.mobile",                      // Kotak Mahindra
        "com.axis.mobile",                           // Axis Bank
        "com.csam.icici.bank.imobile",               // ICICI iMobile
        "com.snapwork.hdfc",                         // HDFC MobileBanking
        "com.indusind.mobile",                       // IndusInd
        "com.idbi.mpassbook",                        // IDBI
        "com.Bank.BOI",                              // Bank of India
        "com.pnbmobilebanking",                      // PNB

        // ── SMS apps — banks still send OTP/alert SMS ──
        // We'll additionally filter by SMS sender inside the text itself.
        "com.google.android.apps.messaging",         // Google Messages
        "com.samsung.android.messaging",             // Samsung Messages
        "com.android.mms",                           // Stock MMS
        "org.thoughtcrime.securesms",                // Signal (rare but possible)
    )

    // ── Known bank/UPI SMS sender IDs that appear in the notification title ──
    // These are the 6-char alphanumeric VM-XXXXX sender IDs used by Indian banks.
    private val BANK_SMS_SENDERS = setOf(
        "HDFCBK", "SBIINB", "ICICIB", "KOTAKB", "AXISBK",
        "INDBNK", "PNBSMS", "BOIIND", "CENTBK", "CANBNK",
        "UNIONB", "SYNDBK", "IDBIBK", "FEDBAK", "YESBNK",
        "PAYTMB", "GPAY",   "PHONEPE","BHIMUPI","CREDCL",
        "SCBNK",  "HSBCIN", "CITIBK", "RBLBNK", "IDFCBK",
        "AUBANK", "LVBNK",  "AMZPAY", "SLICEIT"
    )

    // ── Debit keywords ────────────────────────────────────────────────────────
    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "withdrawn", "payment of", "purchase of", 
        "charged", "deducted", "you paid", "you sent", "paid to", "transferred to"
    )

    // ── Credit keywords ───────────────────────────────────────────────────────
    private val CREDIT_KEYWORDS = listOf(
        "credited", "received", "refund", "cashback", "deposited", 
        "added to", "payment received", "paid you", "sent you", 
        "transferred to you", "money received"
    )

    // ── Noise keywords: drop notification immediately if found ────────────────
    private val NOISE_KEYWORDS = listOf(
        "otp", "one time password", "password", "login", "sign in",
        "verify", "verification", "offer", "discount", "cashback offer",
        "breaking news", "update available", "new message", "missed call"
    )

    // Matches: ₹1,000  |  Rs.500  |  INR 2500  |  Rs 999.00  |  $10.00  |  €50  |  £10
    private val AMOUNT_REGEX = Regex(
        """(?:₹|Rs\.?|INR|\$|€|£|¥)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Extracts a merchant/UPI handle after "to" or "at" or "for" or "on"
    private val MERCHANT_REGEX = Regex(
        """(?:to|at|for|on)\s+([A-Za-z0-9@.\-_ ]{2,40})""",
        RegexOption.IGNORE_CASE
    )

    // Specific pattern for GPay/PhonePe/UPI titles:
    // "Muhammed Zaayid paid you ₹1.00" -> Group 2: Muhammed Zaayid
    // "You paid Muhammed Zaayid ₹1.00" -> Group 1: Muhammed Zaayid
    private val UPI_TITLE_MERCHANT_REGEX = Regex(
        """(?:You paid|Paid to|Sent to)\s+(.*?)\s+(?:₹|Rs|\$|€|£|¥)|^(.*?)\s+(?:paid you|sent you|transferred)\s+(?:₹|Rs|\$|€|£|¥)""",
        RegexOption.IGNORE_CASE
    )

    // ── Entry point ───────────────────────────────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: "unknown"
        Timber.d("New notification from: $packageName (Tracking=$isTrackingEnabled)")

        if (!isTrackingEnabled) return

        // Step 1: Package must be in our allowlist
        if (packageName !in FINANCIAL_PACKAGES) {
            Timber.d("Ignored: $packageName is not in FINANCIAL_PACKAGES")
            return
        }

        val extras = sbn.notification.extras
        
        // Use a helper to safely extract text from any format (Spannable or String)
        fun getSafeText(key: String): String {
            return try {
                extras.getCharSequence(key)?.toString() ?: ""
            } catch (e: Exception) {
                ""
            }
        }

        val title = getSafeText("android.title")
        val text = getSafeText("android.text")
        val bigText = getSafeText("android.bigText")
        val infoText = getSafeText("android.infoText")
        
        // Step 2: For generic SMS apps, sender must look like a bank/UPI ID
        val isSmsApp = packageName in setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
            "org.thoughtcrime.securesms"
        )
        if (isSmsApp && !isFinancialSmsSender(title)) {
            Timber.d("Ignored SMS — sender '$title' not a known bank/UPI sender")
            return
        }

        val full = "$title $text $bigText $infoText".trim()
        val fullLower = full.lowercase()

        Timber.d("Processing: title='%s' text='%s' bigText='%s'", title, text, bigText)

        // Step 3: Drop obvious noise immediately
        if (NOISE_KEYWORDS.any { fullLower.contains(it) }) {
            Timber.d("Ignored — noise keyword matched")
            return
        }

        // Step 4: Must contain a parseable amount
        val amountMatch = AMOUNT_REGEX.find(full) ?: run {
            Timber.d("Ignored — no amount found")
            return
        }
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: run {
            Timber.d("Ignored — amount parse failed")
            return
        }

        // Step 5: Determine debit vs credit
        val isCredit = CREDIT_KEYWORDS.any { fullLower.contains(it) }
        val isDebit  = DEBIT_KEYWORDS.any  { fullLower.contains(it) }

        if (!isDebit && !isCredit) {
            Timber.d("Ignored — no debit/credit keyword found")
            return
        }

        // Prioritize specific phrases to resolve overlaps like "paid" vs "paid you"
        val type = when {
            fullLower.contains("paid you") || fullLower.contains("sent you") || fullLower.contains("received from") -> "credit"
            fullLower.contains("you paid") || fullLower.contains("you sent") || fullLower.contains("paid to") -> "debit"
            isCredit && !isDebit -> "credit"
            else -> "debit"
        }

        // Step 6: Try to extract merchant name
        var merchant = "Unknown Merchant"
        
        // Strategy A: Specific UPI patterns in full text (GPay/PhonePe often put name in title)
        val upiMatch = UPI_TITLE_MERCHANT_REGEX.find(full)
        val extractedFromUpi = upiMatch?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
            ?: upiMatch?.groupValues?.get(2)?.takeIf { it.isNotBlank() }

        if (!extractedFromUpi.isNullOrBlank()) {
            merchant = extractedFromUpi.trim()
        } else {
            // Strategy B: General "to/at/for" regex, excluding common noise words
            val merchantFromText = MERCHANT_REGEX.find(text)?.groupValues?.get(1)?.trim()
            if (!merchantFromText.isNullOrBlank() && !isMerchantNoise(merchantFromText)) {
                merchant = merchantFromText
            } else if (title.isNotBlank() && !isSmsApp) {
                // If it's a financial app, the title is usually the merchant/entity
                merchant = title
            }
        }

        // Step 7: Infer payment mode and save to DB
        val paymentMode = when {
            packageName.contains("nbu.paisa") || packageName.contains("phonepe") || packageName.contains("upiapp") -> "UPI"
            packageName.contains("messaging") || packageName.contains("mms") -> "SMS"
            packageName.contains("paytm") -> "PAYTM"
            else -> "APP"
        }

        val transaction = Transaction(
            amount      = amount,
            type        = type,
            merchant    = merchant,
            dateTime    = sbn.postTime,
            category    = null,
            source      = "NOTIFICATION",
            note        = "[$packageName] $full",
            paymentMode = paymentMode,
            referenceId = "REF-${sbn.postTime}"
        )

        serviceScope.launch {
            try {
                addTransactionUseCase(transaction)
                val currency = preferencesManager.currency.first()
                val formattedAmount = com.credenceai.app.core.utils.CurrencyUtils.formatAmount(amount, currency)
                Timber.d("✅ Saved: %s %s via %s from '%s'", type, formattedAmount, paymentMode, merchant)
            } catch (e: Exception) {
                Timber.e(e, "❌ DB error")
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns true if the name looks like notification UI noise rather than a merchant.
     */
    private fun isMerchantNoise(name: String): Boolean {
        val lower = name.lowercase().trim()
        return lower == "view" || lower == "view." || lower.contains("tap to") || lower.contains("click here")
    }

    /**
     * Returns true if the SMS title (sender ID) looks like an Indian bank /
     * UPI platform — either matches our known set, or follows the VM-XXXXX /
     * AX-XXXXX pattern used by TRAI-registered senders.
     */
    private fun isFinancialSmsSender(sender: String): Boolean {
        val upper = sender.uppercase().trim()
        // Exact match in known senders
        if (upper in BANK_SMS_SENDERS) return true
        // Pattern: two-letter prefix, dash, 4–6 uppercase letters (e.g. VM-HDFCBK)
        if (Regex("""^[A-Z]{2}-[A-Z]{4,6}$""").matches(upper)) return true
        return false
    }

    companion object {
        private const val TAG = "TxnNotifListener"
    }
}