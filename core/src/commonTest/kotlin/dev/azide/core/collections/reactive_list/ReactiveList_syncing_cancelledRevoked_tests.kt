package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.syncing
import dev.azide.core.startExternally
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.schedule.Schedule_cancelled_testUtils
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
@Ignore // TODO: Implement
class ReactiveList_syncing_cancelledRevoked_tests {
    @Test
    fun test_cancelled_sourceUpdates() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_cancelled_sourceUpdates(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_cancelled_sourceUpdates(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )


        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(dispatcher),
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
    fun test_cancelled_sourceUpdatesRevoked() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_cancelled_sourceUpdatesRevoked(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_cancelled_sourceUpdatesRevoked(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(dispatcher),
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
    fun test_cancelled_sourceUpdatesCorrected() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_cancelled_sourceUpdatesCorrected(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_cancelled_sourceUpdatesCorrected(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelled_testUtils.executeCancelTransaction(
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
            ).bind(dispatcher),
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
