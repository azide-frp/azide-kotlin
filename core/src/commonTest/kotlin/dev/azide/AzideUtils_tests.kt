package dev.azide

import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class AzideUtils_tests {
    @Test
    fun test_getAnswer() {
        assertEquals(
            expected = "Azide-?",
            actual = AzideUtils.getAnswer(),
        )
    }
}
