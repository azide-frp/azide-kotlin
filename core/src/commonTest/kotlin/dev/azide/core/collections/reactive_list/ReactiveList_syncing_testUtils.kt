package dev.azide.core.collections.reactive_list

import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.schedule_generic.Schedule_generic_step_testUtils
import kotlin.test.assertEquals

@Suppress("ClassName")
data object ReactiveList_syncing_testUtils {
    fun verifyEffectOngoing(
        sourceReactiveList: TestInputReactiveList<Int>,
        targetMutableList: MutableList<Int>,
    ) {
        val prefix = listOf(0, 1, 2)

        val targetMutableListSnapshot = targetMutableList.toList()

        Schedule_generic_step_testUtils.executeStepTransaction(
            inputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 0,
                        newElements = prefix,
                    ),
                ),
            ),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = prefix + targetMutableListSnapshot,
            actual = targetMutableList,
        )
    }

    fun verifyEffectNotOngoing(
        sourceReactiveList: TestInputReactiveList<Int>,
        targetMutableList: MutableList<Int>,
    ) {
        val targetMutableListSnapshot = targetMutableList.toList()

        Schedule_generic_step_testUtils.executeStepTransaction(
            inputStimulation = sourceReactiveList.change(
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 0,
                        newElements = listOf(-1, -2, -3),
                    ),
                ),
            ),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = targetMutableListSnapshot,
            actual = targetMutableList,
        )
    }
}
