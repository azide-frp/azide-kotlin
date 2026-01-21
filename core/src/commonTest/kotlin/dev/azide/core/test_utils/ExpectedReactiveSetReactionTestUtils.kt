package dev.azide.core.test_utils

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance

typealias ExpectedReactiveSetReaction<ElementT> = ExpectedTestSubjectReaction<ReactiveSet<ElementT>>

object ExpectedReactiveSetReactionTestUtils {
    fun <ElementT> expectChange(
        expectedNewElements: Set<ElementT>,
    ): ExpectedReactiveSetReaction<ElementT> = TODO()

    fun <ElementT> expectNoChange(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedReactiveSetReaction<ElementT> = TODO()
}
