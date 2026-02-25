package dev.azide.core.test_utils.moment

import dev.azide.core.Moment
import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestStimulation

// Minimal test input moment implementation to allow setting moment values in tests
class TestInputMoment<ValueT>(
    initialValue: ValueT,
) : Moment<ValueT> {
    var currentValue: ValueT = initialValue
        private set

    override fun pullInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): ValueT = currentValue

    fun setCurrentValue(newValue: ValueT) {
        currentValue = newValue
    }

    fun setValue(newValue: ValueT): TestStimulation = object : TestStimulation {
        override fun stimulate(propagationContext: Transactions.PropagationContext) {
            currentValue = newValue
        }
    }
}

// Adapter helper
fun <ValueT> TestInputMoment<ValueT>.emittingValue(value: ValueT): TestStimulation = setValue(value)
