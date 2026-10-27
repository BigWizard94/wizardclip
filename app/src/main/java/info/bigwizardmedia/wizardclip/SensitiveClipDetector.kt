package info.bigwizardmedia.wizardclip

/**
 * Pure-Kotlin detector for sensitive clipboard content.
 *
 * Kept free of any Android dependency so it can be unit-tested on the JVM.
 * Detection is intentionally conservative: a clip is only flagged when it
 * looks like an OTP code, a labelled password/secret, or a Luhn-valid
 * card-like number. Ordinary prose, links and phone numbers are not flagged.
 */
object SensitiveClipDetector {

    /** How long a sensitive clip lives before it is auto-deleted. */
    const val SENSITIVE_TTL_MS: Long = 5L * 60L * 1000L

    enum class Kind(val label: String) {
        OTP("OTP code"),
        PASSWORD("Password-like"),
        CARD("Card number"),
    }

    private val otpLabelPattern = Regex(
        """(?i)\b(otp|one[\s-]?time\s+(?:pass(?:word|code)|code)|verification\s+code|auth(?:entication)?\s+code|2fa\s+code|security\s+code|code)\b[^0-9]{0,20}(\d{4,8})\b"""
    )
    private val standaloneCodePattern = Regex("""^\d{4,8}$""")
    private val passwordPattern = Regex(
        """(?i)\b(password|passwd|pwd|pass|secret|api[_\s-]?key|access[_\s-]?token|auth[_\s-]?token)\b\s*[:=]\s*\S{4,}"""
    )
    private val cardCandidatePattern = Regex("""\b(?:\d[ -]?){13,19}\b""")

    /** Returns the kind of sensitive content found, or null when none is found. */
    fun detect(text: String): Kind? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        if (findCardNumber(trimmed) != null) return Kind.CARD
        if (otpLabelPattern.containsMatchIn(trimmed) || standaloneCodePattern.matches(trimmed)) {
            return Kind.OTP
        }
        if (passwordPattern.containsMatchIn(trimmed)) return Kind.PASSWORD
        return null
    }

    fun isSensitive(text: String): Boolean = detect(text) != null

    /** Returns the digit string of a Luhn-valid card-like number, or null. */
    fun findCardNumber(text: String): String? {
        for (match in cardCandidatePattern.findAll(text)) {
            val digits = match.value.filter { it.isDigit() }
            if (digits.length !in 13..19) continue
            if (digits.all { it == digits[0] }) continue
            if (passesLuhn(digits)) return digits
        }
        return null
    }

    internal fun passesLuhn(digits: String): Boolean {
        var sum = 0
        var doubleNext = false
        for (i in digits.length - 1 downTo 0) {
            var digit = digits[i] - '0'
            if (doubleNext) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
            doubleNext = !doubleNext
        }
        return digits.isNotEmpty() && sum % 10 == 0
    }
}
