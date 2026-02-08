package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.syncing
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x2
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_start_testUtils
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class ReactiveList_syncing_start_tests {
    @Test
    fun test_start() {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(10, 20, 30),
            actual = targetMutableList,
        )
    }

    @Test
    fun test_start_sourceUpdatesSimultaneously() {
        TestSlottedStimulationScenario1x2.entries.forEach { slottedStimulationScenario ->
            test_start_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_sourceUpdatesSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario1x2,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_testUtils.executeStartTransaction(
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
            expected = listOf(10, 20, 21, 22, 23, 30),
            actual = targetMutableList,
        )
    }

    @Test
    fun test_start_sourceUpdatesRevokedSimultaneously() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_start_sourceUpdatesRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_sourceUpdatesRevokedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_testUtils.executeStartTransaction(
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
            expected = listOf(10, 20, 30),
            actual = targetMutableList,
        )
    }

    @Test
    fun test_start_sourceUpdatesCorrectedSimultaneously() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_start_sourceUpdatesCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_sourceUpdatesCorrectedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_testUtils.executeStartTransaction(
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
            expected = listOf(10, 20, 24, 25, 30),
            actual = targetMutableList,
        )
    }
}
