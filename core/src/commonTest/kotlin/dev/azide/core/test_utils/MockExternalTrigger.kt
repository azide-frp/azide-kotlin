package dev.azide.core.test_utils

import dev.azide.core.external.ExternalTrigger

class MockExternalTrigger : ExternalTrigger {
    private var mutableWasCalled = false

    val wasCalled: Boolean
        get() = mutableWasCalled

    override fun executeExternally() {
        mutableWasCalled = true
    }
}
