package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.Update
import dev.azide.core.impl.cell.abstract_vertices.AbstractBaseStatefulCellVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestInputEntity
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.semantic.AnySemanticCell
import dev.azide.core.test_utils.semantic.SemanticCell
import dev.azide.core.test_utils.semantic.Timestamp
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap_deprecated

class TestInputCell<ValueT>(
    initialValue: ValueT,
) : Cell<ValueT>, TestInputEntity {
    interface ValueRealizer<in SemanticValueT, out RealValueT> {
        fun realize(
            semanticValue: SemanticValueT,
        ): RealValueT
    }

    companion object {
        fun <ValueT> realizeInitially(
            semanticCell: AnySemanticCell<ValueT>,
        ): TestInputCell<ValueT> {
            val initialSemanticValueSnapshot: SemanticCell.ValueSnapshot<ValueT> = semanticCell.evaluate(
                timestamp = Timestamp.zero,
            )

            return TestInputCell(
                initialValue = initialSemanticValueSnapshot.value,
            )
        }

        fun <SemanticValueT, RealValueT> realizeInitially(
            semanticCell: AnySemanticCell<SemanticValueT>,
            valueRealizer: ValueRealizer<SemanticValueT, RealValueT>,
        ): TestInputCell<RealValueT> {
            val initialSemanticValueSnapshot: SemanticCell.ValueSnapshot<SemanticValueT> = semanticCell.evaluate(
                timestamp = Timestamp.zero,
            )

            val initialRealValue: RealValueT = valueRealizer.realize(
                semanticValue = initialSemanticValueSnapshot.value,
            )

            return TestInputCell(
                initialValue = initialRealValue,
            )
        }
    }

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
    ): TestStimulation = object : TestStimulation {
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
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.correctUpdate(
                propagationContext = propagationContext,
                correctedNewValue = correctedNewValue,
            )
        }
    }

    fun revokeUpdate(): TestStimulation = object : TestStimulation {
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

    override val testVertex: ListenableVertex
        get() = _vertex
}

fun <ValueT> TestInputCell<ValueT>.updating_deprecated(
    tag: TestInputCellTag,
    newValue: ValueT,
): TestStimulationMap_deprecated = TestStimulationMap_deprecated.of(
    TestInputCellStimulationTag.Update(inputTag = tag) to update(
        newValue = newValue,
    ),
)

fun <ValueT> TestInputCell<ValueT>.revokingUpdate_deprecated(
    newValue: ValueT,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = update(newValue = newValue),
    secondStimulation = revokeUpdate(),
)

fun <ValueT> TestInputCell<ValueT>.revokingUpdate_deprecated(
    tag: TestInputCellTag,
    newValue: ValueT,
): TestStimulationMap_deprecated = revokingUpdate_deprecated(
    newValue = newValue,
).tagged(
    firstTag = TestInputCellStimulationTag.Update(inputTag = tag),
    secondTag = TestInputCellStimulationTag.UpdateRevocation(inputTag = tag),
)

fun <ValueT> TestInputCell<ValueT>.correctingUpdate_deprecated(
    intermediateNewValue: ValueT,
    correctedNewValue: ValueT,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = update(newValue = intermediateNewValue),
    secondStimulation = correctUpdate(correctedNewValue),
)

fun <ValueT> TestInputCell<ValueT>.correctingUpdate_deprecated(
    tag: TestInputCellTag,
    intermediateNewValue: ValueT,
    correctedNewValue: ValueT,
): TestStimulationMap_deprecated = correctingUpdate_deprecated(
    intermediateNewValue = intermediateNewValue,
    correctedNewValue = correctedNewValue,
).tagged(
    firstTag = TestInputCellStimulationTag.Update(inputTag = tag),
    secondTag = TestInputCellStimulationTag.UpdateCorrection(inputTag = tag),
)
