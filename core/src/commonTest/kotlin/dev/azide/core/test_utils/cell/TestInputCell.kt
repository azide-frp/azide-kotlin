package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.Update
import dev.azide.core.impl.cell.abstract_vertices.AbstractBaseStatefulCellVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestInputStimulation

class TestInputCell<ValueT>(
    initialValue: ValueT,
) : Cell<ValueT> {
    private val _vertex = object : AbstractBaseStatefulCellVertex<ValueT>(
        initialValue = initialValue,
    ) {
        fun update(
            propagationContext: Transactions.PropagationContext,
            newValue: ValueT,
        ) {
            if (ongoingUpdate != null) {
                throw IllegalStateException("Another update is already ongoing")
            }

            exposeUpdateNotifyingListeners(
                propagationContext = propagationContext,
                update = Update(
                    updatedValue = newValue,
                ),
            )
        }

        fun correctUpdate(
            propagationContext: Transactions.PropagationContext,
            correctedNewValue: ValueT,
        ) {
            if (ongoingUpdate == null) {
                throw IllegalStateException("No ongoing update to correct")
            }

            exposeUpdateNotifyingListeners(
                propagationContext = propagationContext,
                update = Update(
                    updatedValue = correctedNewValue,
                ),
            )
        }

        fun revokeUpdate(
            propagationContext: Transactions.PropagationContext,
        ) {
            if (ongoingUpdate == null) {
                throw IllegalStateException("No ongoing update to revoke")
            }

            exposeUpdateNotifyingListeners(
                propagationContext = propagationContext,
                update = null,
            )
        }
    }

    fun update(
        newValue: ValueT,
    ): TestInputStimulation = object : TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.update(
                propagationContext = propagationContext,
                newValue = newValue,
            )
        }
    }

    fun correctUpdate(
        correctedNewValue: ValueT,
    ): TestInputStimulation = object : TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.correctUpdate(
                propagationContext = propagationContext,
                correctedNewValue = correctedNewValue,
            )
        }
    }

    fun revokeUpdate(): TestInputStimulation = object : TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.revokeUpdate(
                propagationContext = propagationContext,
            )
        }
    }

    override val vertex: CellVertex<ValueT>
        get() = _vertex
}

fun <ValueT> TestInputCell<ValueT>.revokingUpdate(
    newValue: ValueT,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = update(newValue = newValue),
    secondStimulation = revokeUpdate(),
)

fun <ValueT> TestInputCell<ValueT>.correctingUpdate(
    intermediateNewValue: ValueT,
    correctedNewValue: ValueT,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = update(newValue = intermediateNewValue),
    secondStimulation = correctUpdate(correctedNewValue),
)
