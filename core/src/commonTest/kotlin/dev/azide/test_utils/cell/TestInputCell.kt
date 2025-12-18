package dev.azide.test_utils.cell

import dev.azide.Cell
import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex
import dev.azide.internal.cell.CellVertex.Update
import dev.azide.internal.cell.abstract_vertices.AbstractStatefulCellVertex
import dev.azide.test_utils.TestInputStimulation

internal class TestInputCell<ValueT>(
    initialValue: ValueT,
) : Cell<ValueT> {
    private val _vertex = object : AbstractStatefulCellVertex<ValueT>(
        initialValue = initialValue,
    ) {
        fun update(
            propagationContext: Transactions.PropagationContext,
            newValue: ValueT,
        ) {
            if (ongoingUpdate != null) {
                throw IllegalStateException("Another update is already ongoing")
            }

            exposeAndPropagateUpdate(
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

            exposeAndPropagateUpdate(
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

            exposeAndPropagateUpdate(
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

    override fun getVertex(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex<ValueT> = _vertex
}
