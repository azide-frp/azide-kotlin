package dev.azide.core.test_utils

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance

interface ExpectedReactiveBagChange<ElementT> : ExpectedTestSubjectReaction<ReactiveBag<ElementT>>

interface ExpectedReactiveBagValue<ElementT> : ExpectedTestSubjectState<ReactiveBag<ElementT>>

interface ExpectedReactiveBagContentTransition<ElementT> : ExpectedTestSubjectTransition<ReactiveBag<ElementT>>

@Suppress("ClassName")
object ReactiveBag_expectations_testUtils {
    fun <ElementT> expectTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldTaggedContent: Map<Tag, ElementT>,
        expectedNewTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> = TODO()

    fun <ElementT> expectNoTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUnaffectedTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> = TODO()

    fun <ElementT> expectStableTaggedContent(
        expectedTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagValue<ElementT> = TODO()
}
