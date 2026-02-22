package dev.azide.core.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.map
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils.SourceCellTag
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_sampling_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_reaction_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

/**
 * Tests for [dev.azide.core.collections.ReactiveList.of] overload accepting a single [dev.azide.core.Cell].
 */
@Suppress("ClassName", "PrivatePropertyName")
class ReactiveList_ofSingle_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceCellUpdates =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesRevoked =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesCorrected =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_passiveSample() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectReactiveList = ReactiveList.of(sourceCell)

        ReactiveList_sampling_testUtils.executeSamplingTransaction(
            subjectReactiveList = subjectReactiveList,
            expectedSubjectContent = ReactiveList_expectations_testUtils.expectStableContent(
                expectedContent = listOf(10),
            ),
        )
    }

    @Test
    fun test_sourceCellUpdates() {
        slottedStimulationScenarioBank_sourceCellUpdates.forEach {
            test_sourceCellUpdates(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCellUpdates(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectReactiveList = ReactiveList.of(sourceCell)

        ReactiveList_reaction_testUtils.testReaction(
            subjectReactiveList,
            slottedInputStimulation = sourceCell.updating(
                tag = SourceCellTag,
                newValue = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf(10),
                expectedNewContent = listOf(11),
            ),
        )
    }

    @Test
    fun test_sourceCellUpdatesRevoked() {
        slottedStimulationScenarioBank_sourceCellUpdatesRevoked.forEach {
            test_sourceCellUpdatesRevoked(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCellUpdatesRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectReactiveList = ReactiveList.of(sourceCell)

        ReactiveList_reaction_testUtils.testReaction(
            subjectReactiveList,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceCellTag,
                newValue = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf(10),
            ),
        )
    }

    @Test
    fun test_sourceCellUpdatesCorrected() {
        slottedStimulationScenarioBank_sourceCellUpdatesCorrected.forEach {
            test_sourceCellUpdatesCorrected(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCellUpdatesCorrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectReactiveList = ReactiveList.of(sourceCell)

        ReactiveList_reaction_testUtils.testReaction(
            subjectReactiveList,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = SourceCellTag,
                intermediateNewValue = 11,
                correctedNewValue = 12,
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf(10),
                expectedNewContent = listOf(12),
            ),
        )
    }
}
