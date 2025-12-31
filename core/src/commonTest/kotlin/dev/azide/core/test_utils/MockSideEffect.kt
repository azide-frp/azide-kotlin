package dev.azide.core.test_utils

import dev.azide.core.ExternalSideEffect

class MockSideEffect() : ExternalSideEffect {
    private var mutableWasCalled = false

    val wasCalled: Boolean
        get() = mutableWasCalled

    override fun executeExternally() {
        mutableWasCalled = true
    }
}
