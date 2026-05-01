package com.credenceai.app.data.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.credenceai.app.domain.model.Transaction
import com.credenceai.app.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var addTransactionUseCase: AddTransactionUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        "debited", "debit", "spent", "paid", "sent", "withdrawn",
        "payment of", "purchase of", "charged", "deducted"
    )

    // ── Credit keywords ───────────────────────────────────────────────────────
    private val CREDIT_KEYWORDS = listOf(
        "credited", "credit", "received", "refund", "cashback",
        "deposited", "added to", "payment received"
    )

    // ── Noise keywords: drop notification immediately if found ────────────────
    private val NOISE_KEYWORDS = listOf(
        "otp", "one time password", "password", "login", "sign in",
        "verify", "verification", "offer", "discount", "cashback offer",
        "breaking news", "update available", "new message", "missed call"
    )

    // Matches: ₹1,000  |  Rs.500  |  INR 2500  |  Rs 999.00
    private val AMOUNT_REGEX = Regex(
        """(?:₹|Rs\.?\s*|INR\s*)([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Extracts a merchant/UPI handle after "to" or "at" or "for"
    private val MERCHANT_REGEX = Regex(
        """(?:to|at|for)\s+([A-Za-z0-9@.\-_ ]{2,40})""",
        RegexOption.IGNORE_CASE
    )

    // ── Entry point ───────────────────────────────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return

        // Step 1: Package must be in our allowlist
        if (packageName !in FINANCIAL_PACKAGES) return

        val extras = sbn.notification.extras
        val title  = extras.getString("android.title")?.trim() ?: ""
        val text   = extras.getString("android.text")?.trim()  ?: ""
        val full   = "$title $text"

        Log.d(TAG, "[$packageName] title='$title' text='$text'")

        // Step 2: For generic SMS apps, sender must look like a bank/UPI ID
        val isSmsApp = packageName in setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
            "org.thoughtcrime.securesms"
        )
        if (isSmsApp && !isFinancialSmsSender(title)) {
            Log.d(TAG, "Ignored SMS — sender '$title' not a known bank/UPI sender")
            return
        }

        // Step 3: Drop obvious noise immediately
        val fullLower = full.lowercase()
        if (NOISE_KEYWORDS.any { fullLower.contains(it) }) {
            Log.d(TAG, "Ignored — noise keyword matched")
            return
        }

        // Step 4: Must contain a parseable amount
        val amountMatch = AMOUNT_REGEX.find(full) ?: run {
            Log.d(TAG, "Ignored — no amount found")
            return
        }
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: run {
            Log.d(TAG, "Ignored — amount parse failed")
            return
        }

        // Step 5: Determine debit vs credit
        val isDebit  = DEBIT_KEYWORDS.any  { fullLower.contains(it) }
        val isCredit = CREDIT_KEYWORDS.any { fullLower.contains(it) }

        if (!isDebit && !isCredit) {
            Log.d(TAG, "Ignored — no debit/credit keyword found")
            return
        }

        val type = if (isCredit && !isDebit) "credit" else "debit"

        // Step 6: Try to extract merchant name
        val merchantFromText = MERCHANT_REGEX.find(text)?.groupValues?.get(1)?.trim()
        val merchant = when {
            !merchantFromText.isNullOrBlank() -> merchantFromText
            title.isNotBlank()               -> title
            else                             -> "Unknown Merchant"
        }

        // Step 7: Save to DB
        val transaction = Transaction(
            amount      = amount,
            type        = type,
            merchant    = merchant,
            dateTime    = sbn.postTime,
            category    = null,
            source      = "NOTIFICATION",
            note        = "[$packageName] $text",
            paymentMode = null,
            referenceId = "REF-${sbn.postTime}"
        )

        serviceScope.launch {
            try {
                addTransactionUseCase(transaction)
                Log.d(TAG, "✅ Saved: $type ₹$amount from '$merchant'")
            } catch (e: Exception) {
                Log.e(TAG, "❌ DB error", e)
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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