package info.bigwizardmedia.wizardclip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveClipDetectorTest {

    @Test
    fun cardNumberIsSensitive() {
        assertEquals(
            SensitiveClipDetector.Kind.CARD,
            SensitiveClipDetector.detect("4242 4242 4242 4242"),
        )
        assertTrue(SensitiveClipDetector.isSensitive("Card: 4111-1111-1111-1111"))
    }

    @Test
    fun invalidCardNumberIsNotSensitive() {
        assertNull(SensitiveClipDetector.detect("4242 4242 4242 4241"))
        assertNull(SensitiveClipDetector.detect("Call me on 4242 4242"))
    }

    @Test
    fun otpCodeIsSensitive() {
        assertEquals(
            SensitiveClipDetector.Kind.OTP,
            SensitiveClipDetector.detect("Your verification code is 483920"),
        )
        assertEquals(
            SensitiveClipDetector.Kind.OTP,
            SensitiveClipDetector.detect("483920"),
        )
    }

    @Test
    fun shortNumbersAreNotSensitive() {
        assertNull(SensitiveClipDetector.detect("My code is 123"))
        assertNull(SensitiveClipDetector.detect("Meet at 5pm, bring 2 pizzas"))
    }

    @Test
    fun passwordLabelIsSensitive() {
        assertEquals(
            SensitiveClipDetector.Kind.PASSWORD,
            SensitiveClipDetector.detect("password: hunter2hunter"),
        )
        assertTrue(SensitiveClipDetector.isSensitive("api_key=abcdef123456"))
    }

    @Test
    fun ordinaryTextIsNotSensitive() {
        assertFalse(SensitiveClipDetector.isSensitive("Thanks for reaching out!"))
        assertFalse(SensitiveClipDetector.isSensitive("https://bigwizardmedia.biz"))
        assertNull(SensitiveClipDetector.detect(""))
        assertNull(SensitiveClipDetector.detect("   "))
    }

    @Test
    fun luhnCheck() {
        assertTrue(SensitiveClipDetector.passesLuhn("4242424242424242"))
        assertFalse(SensitiveClipDetector.passesLuhn("4242424242424241"))
    }
}
