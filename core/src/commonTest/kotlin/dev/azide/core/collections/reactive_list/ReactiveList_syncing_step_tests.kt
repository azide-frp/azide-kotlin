package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.syncing
import dev.azide.core.startExternally
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.schedules.ScheduleTestUtils_step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.Ignore

@Suppress("ClassName")
@Ignore // TODO: Implement
class ReactiveList_syncing_step_tests {
    @Test
    fun test_step_sourceUpdates_insertion() {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        subjectSchedule.startExternally()

        ScheduleTestUtils_step.executeStepTransaction(
            inputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(0, 10, 20, 21, 22, 23, 30),
            actual = targetMutableList,
        )
    }

    @Test
    fun test_step_sourceUpdates_removal() {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        subjectSchedule.startExternally()

        ScheduleTestUtils_step.executeStepTransaction(
            inputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Removal(
                        indexRange = 2..3,
                    ),
                ),
            ),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )
        assertEquals(
            expected = listOf(0, 10, 40, 50),
            actual = targetMutableList,
        )
    }

    @Test
    fun test_step_sourceUpdates_replacement() {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        subjectSchedule.startExternally()

        ScheduleTestUtils_step.executeStepTransaction(
            inputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Replacement(
                        indexRange = 3..5,
                        replacedElements = listOf(31, 32),
                    ),
                ),
            ),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )
        assertEquals(
            expected = listOf(0, 10, 20, 31, 32, 60),
            actual = targetMutableList,
        )
    }

    @Test
    fun test_step_sourceUpdatesRevoked() {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        subjectSchedule.startExternally()

        ScheduleTestUtils_step.executeStepTransaction(
            inputStimulation = sourceReactiveList.revokingChange(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).joint(),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(0, 10, 20, 30),
            actual = targetMutableList,
        )
    }

    @Test
    fun test_step_sourceUpdatesCorrected() {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        subjectSchedule.startExternally()

        ScheduleTestUtils_step.executeStepTransaction(
            inputStimulation = sourceReactiveList.correctingChange(
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
            ).joint(),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(0, 10, 14, 15, 20, 30),
            actual = targetMutableList,
        )
    }
}
