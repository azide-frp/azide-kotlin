package dev.azide.core.cell

import dev.azide.core.test_utils.cell.Cell_generic_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils.SourceCellTag
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.updatedValues
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_updatedValues_tests {

    @Test
    fun test_sourceUpdates_observed() {
        test_sourceUpdates(placementObserved = true)
    }

    @Test
    fun test_sourceUpdates_unobserved() {
        test_sourceUpdates(placementObserved = false)
    }

    private fun test_sourceUpdates(
        placementObserved: Boolean,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        val inputPlan = if (placementObserved) {
            generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = sourceCell.update(newValue = 20),
            )
        } else {
            generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = sourceCell.update(newValue = 20),
                observedInputStimulation = TestStimulation.Noop,
            )
        }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = inputPlan,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 20,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_revoked_observed() {
        test_sourceUpdates_revoked(placementObserved = true)
    }

    @Test
    fun test_sourceUpdates_revoked_unobserved() {
        test_sourceUpdates_revoked(placementObserved = false)
    }

    private fun test_sourceUpdates_revoked(
        placementObserved: Boolean,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        val inputPlan = if (placementObserved) {
            generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = dev.azide.core.test_utils.DoubleTestStimulation(
                    firstStimulation = sourceCell.update(newValue = 20),
                    secondStimulation = sourceCell.revokeUpdate(),
                ).joint(),
            )
        } else {
            generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = dev.azide.core.test_utils.DoubleTestStimulation(
                    firstStimulation = sourceCell.update(newValue = 20),
                    secondStimulation = sourceCell.revokeUpdate(),
                ).joint(),
                observedInputStimulation = TestStimulation.Noop,
            )
        }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = inputPlan,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected_observed() {
        test_sourceUpdates_corrected(placementObserved = true)
    }

    @Test
    fun test_sourceUpdates_corrected_unobserved() {
        test_sourceUpdates_corrected(placementObserved = false)
    }

    private fun test_sourceUpdates_corrected(
        placementObserved: Boolean,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        val inputPlan = if (placementObserved) {
            generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = dev.azide.core.test_utils.DoubleTestStimulation(
                    firstStimulation = sourceCell.update(newValue = 20),
                    secondStimulation = sourceCell.correctUpdate(correctedNewValue = 21),
                ).joint(),
            )
        } else {
            generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = dev.azide.core.test_utils.DoubleTestStimulation(
                    firstStimulation = sourceCell.update(newValue = 20),
                    secondStimulation = sourceCell.correctUpdate(correctedNewValue = 21),
                ).joint(),
                observedInputStimulation = TestStimulation.Noop,
            )
        }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = inputPlan,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 21,
            ),
        )
    }
}
