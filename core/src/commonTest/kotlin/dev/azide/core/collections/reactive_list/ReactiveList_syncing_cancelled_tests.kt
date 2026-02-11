package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.reactive_list.ReactiveList_generic_testUtils.SourceReactiveListTag
import dev.azide.core.collections.syncing
import dev.azide.core.startExternally
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.changing
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_cancelled_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveList_syncing_cancelled_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceListChanges =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChanges.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceListChangesRevoked =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceListChangesCorrected =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChangesCorrected.distribute(slotCount = SuitableSlotCount)

    private val initialSourceElements = listOf(0, 10, 20, 30)

    @Test
    fun test_cancelled_sourceUpdates() {
        slottedStimulationBank_sourceListChanges.forEach { slottedStimulationScenario ->
            test_cancelled_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelled_sourceUpdates(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = initialSourceElements,
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.changing(
                tag = SourceReactiveListTag,
                changeDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = initialSourceElements,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_cancelled_sourceUpdatesRevoked() {
        slottedStimulationBank_sourceListChangesRevoked.forEach { slottedStimulationScenario ->
            test_cancelled_sourceUpdatesRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelled_sourceUpdatesRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = initialSourceElements,
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                tag = SourceReactiveListTag,
                temporaryChangeDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = initialSourceElements,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_cancelled_sourceUpdatesCorrected() {
        slottedStimulationBank_sourceListChangesCorrected.forEach { slottedStimulationScenario ->
            test_cancelled_sourceUpdatesCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelled_sourceUpdatesCorrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = initialSourceElements,
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                tag = SourceReactiveListTag,
                intermediateChangeDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
                correctedChangeDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(14, 15),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = initialSourceElements,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }
}
