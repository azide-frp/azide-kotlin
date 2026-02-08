package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.syncing
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x3
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_startRevoked_testUtils
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class ReactiveList_syncing_startRevoked_tests {
    val originalTargetListContent = listOf(-1, -2, -3)

    @Test
    fun test_startRevoked() {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesSimultaneously() {
        TestSlottedStimulationScenario1x3.entries.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario1x3,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesRevokedSimultaneously() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesRevokedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesCorrectedSimultaneously() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesCorrectedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
                correctedDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(24, 25),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }
}
