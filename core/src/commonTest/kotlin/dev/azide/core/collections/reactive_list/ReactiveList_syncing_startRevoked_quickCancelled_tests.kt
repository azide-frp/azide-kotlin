package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.syncing
import dev.azide.core.test_utils.TestSlotDispatcher1x4
import dev.azide.core.test_utils.TestSlotDispatcher2x4
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_startRevoked_quickCancelled_testUtils
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class ReactiveList_syncing_startRevoked_quickCancelled_tests {
    val originalTargetListContent = listOf(-1, -2, -3)

    @Test
    fun test_startRevoked_quickCancelled() {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_quickCancelled_testUtils.executeStartTransaction(
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
    fun test_startRevoked_quickCancelled_sourceUpdatesSimultaneously() {
        TestSlotDispatcher1x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceUpdatesSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceUpdatesSimultaneously(
        dispatcher: TestSlotDispatcher1x4,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(dispatcher),
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
    fun test_startRevoked_quickCancelled_sourceUpdatesRevokedSimultaneously() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceUpdatesRevokedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceUpdatesRevokedSimultaneously(
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(dispatcher),
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
    fun test_startRevoked_quickCancelled_sourceUpdatesCorrectedSimultaneously() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceUpdatesCorrectedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceUpdatesCorrectedSimultaneously(
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_quickCancelled_testUtils.executeStartTransaction(
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
            ).bind(dispatcher),
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
