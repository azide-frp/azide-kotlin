package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.syncing
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x3
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_cancelledRevoked_testUtils
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class ReactiveList_syncing_cancelledRevoked_tests {
    @Test
    fun test_cancelledRevoked_sourceUpdates() {
        TestSlottedStimulationScenario1x3.entries.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelledRevoked_sourceUpdates(
        slottedStimulationScenario: TestSlottedStimulationScenario1x3,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
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
            expected = listOf(0, 10, 20, 21, 22, 23, 30),
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesRevoked() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceUpdatesRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelledRevoked_sourceUpdatesRevoked(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                description = ChangeDescription.of(
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
            expected = listOf(0, 10, 20, 30),
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesCorrected() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceUpdatesCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelledRevoked_sourceUpdatesCorrected(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
                correctedDescription = ChangeDescription.of(
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
            expected = listOf(0, 10, 14, 15, 20, 30),
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }
}
