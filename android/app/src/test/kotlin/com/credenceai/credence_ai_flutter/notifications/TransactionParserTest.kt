package com.credenceai.credence_ai_flutter.notifications

import org.junit.Assert.*
import org.junit.Test

class TransactionParserTest {
    @Test fun parsesUpiDebitWithExactMinorUnits() {
        val result = TransactionParser.parse("com.phonepe.app", "You paid Coffee Shop ₹1,250.50", "Payment successful", 1000, "key")!!
        assertEquals(125050L, result["amount_minor"])
        assertEquals("debit", result["type"])
        assertEquals("Coffee Shop", result["merchant"])
    }
    @Test fun incomingUpiPaymentIsCredit() {
        val result = TransactionParser.parse("com.phonepe.app", "Alex paid you ₹20.00", "Money received", 1000, "key")!!
        assertEquals("credit", result["type"])
        assertEquals("Alex", result["merchant"])
    }
    @Test fun ignoresOtpAndUnknownPackagesAndPersonalSms() {
        assertNull(TransactionParser.parse("com.phonepe.app", "OTP", "Use OTP 12345 for payment of Rs 200", 1000, "a"))
        assertNull(TransactionParser.parse("untrusted.app", "You paid ₹100", "", 1000, "b"))
        assertNull(TransactionParser.parse("com.google.android.apps.messaging", "Friend", "You paid Rs 100", 1000, "c"))
    }
    @Test fun parsesKnownBankSenderAndDeduplicatesReferenceAcrossNotifications() {
        val first = TransactionParser.parse("com.google.android.apps.messaging", "VM-HDFCBK", "Rs.100.00 debited at Shop UPI Ref 123456789012", 1000, "a")!!
        val second = TransactionParser.parse("com.google.android.apps.messaging", "HDFCBK", "Rs.100.00 debited at Shop UPI Ref 123456789012", 2000, "b")!!
        assertEquals(first["id"], second["id"])
    }
}
