package dev.azide.core.test_utils

import dev.azide.core.impl.Vertex
import kotlin.test.assertEquals

interface TestInputEntity {
    val testVertex: Vertex
}

val TestInputEntity.listenerCount: Int
    get() = testVertex.listenerCount

fun assertIsInactive(
    testInputEntity: TestInputEntity,
    inputEntityLabel: String,
) {
    assertEquals(
        expected = 0,
        actual = testInputEntity.listenerCount,
        message = "Expected the input entity ($inputEntityLabel) to be inactive, but it has ${testInputEntity.listenerCount} listeners",
    )
}
