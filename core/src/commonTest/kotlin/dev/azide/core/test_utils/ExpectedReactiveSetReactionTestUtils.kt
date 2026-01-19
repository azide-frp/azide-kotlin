package dev.azide.core.test_utils

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex.SetChange
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

typealias ExpectedReactiveSetReaction<ElementT> = ExpectedTestSubjectReaction<ReactiveSet<ElementT>>

object ExpectedReactiveSetReactionTestUtils {
    fun <ElementT> expectChange(
        expectedNewElements: Set<ElementT>,
    ): ExpectedReactiveSetReaction<ElementT> = TODO()

    fun <ElementT> expectNoChange(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedReactiveSetReaction<ElementT> = TODO()
}
