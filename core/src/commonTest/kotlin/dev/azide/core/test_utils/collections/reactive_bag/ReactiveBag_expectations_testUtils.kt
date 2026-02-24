package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.test_utils.generic.AbstractExplicitExpectedTestSubjectReaction
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.ExpectedTestSubjectState
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import kotlin.test.assertEquals

typealias ExpectedBasicReactiveBagChange<ElementT> = AbstractExplicitExpectedTestSubjectReaction<ReactiveBag<ElementT>, TaggedBagChange<ElementT>>

interface ExpectedReactiveBagContent<ElementT> : ExpectedTestSubjectState<ReactiveBag<ElementT>>

interface ExpectedReactiveBagContentTransition<ElementT> :
    ExpectedTestSubjectTransition<ReactiveBag<ElementT>, TaggedBagChange<ElementT>>

private abstract class AbstractExpectedReactiveBagChange<ElementT> : ExpectedBasicReactiveBagChange<ElementT>() {
    final override val expectedSubjectNotification: TaggedBagChange<ElementT>?
        get() = expectedEffectiveChange

    abstract val expectedEffectiveChange: TaggedBagChange<ElementT>?
}

abstract class AbstractExpectedReactiveBagContentTransition<ElementT> : ExpectedReactiveBagContentTransition<ElementT> {
    final override val expectedOldState: ExpectedReactiveBagContent<ElementT>
        get() = ReactiveBag_expectations_testUtils.expectStableTaggedContent(
            expectedTaggedContent = expectedOldTaggedContent,
        )

    final override val expectedNewState: ExpectedReactiveBagContent<ElementT>
        get() = ReactiveBag_expectations_testUtils.expectStableTaggedContent(
            expectedTaggedContent = expectedNewTaggedContent,
        )

    abstract val expectedOldTaggedContent: Map<Tag, ElementT>

    abstract val expectedNewTaggedContent: Map<Tag, ElementT>
}

@Suppress("ClassName")
object ReactiveBag_expectations_testUtils {
    fun <ElementT> expectTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldTaggedElements: TaggedBag<ElementT>,
        expectedNewTaggedElements: TaggedBag<ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> = expectTaggedContentTransition(
        intermediatePropagationTolerance = intermediatePropagationTolerance,
        expectedOldTaggedContent = expectedOldTaggedElements.elementByTag,
        expectedNewTaggedContent = expectedNewTaggedElements.elementByTag,
    )

    fun <ElementT> expectTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldTaggedContent: Map<Tag, ElementT>,
        expectedNewTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> =
        object : AbstractExpectedReactiveBagContentTransition<ElementT>() {
            override val expectedOldTaggedContent: Map<Tag, ElementT> = expectedOldTaggedContent

            override val expectedNewTaggedContent: Map<Tag, ElementT> = expectedNewTaggedContent

            override val expectedReaction: ExpectedBasicReactiveBagChange<ElementT> =
                object : AbstractExpectedReactiveBagChange<ElementT>() {
                    override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
                        intermediatePropagationTolerance

                    override val expectedEffectiveChange: TaggedBagChange<ElementT>? = TaggedBagChange_testUtils.diff(
                        oldTaggedContent = expectedOldTaggedContent,
                        newTaggedContent = expectedNewTaggedContent,
                    )
                }
        }

    fun <ElementT> expectPotentialTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldTaggedElements: TaggedBag<ElementT>,
        expectedNewTaggedElements: TaggedBag<ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> = when {
        expectedOldTaggedElements == expectedNewTaggedElements -> expectNoTaggedContentTransition(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
            expectedUnaffectedTaggedElements = expectedOldTaggedElements,
        )

        else -> expectTaggedContentTransition(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
            expectedOldTaggedElements = expectedOldTaggedElements,
            expectedNewTaggedElements = expectedNewTaggedElements,
        )
    }

    fun <ElementT> expectNoTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUnaffectedTaggedElements: TaggedBag<ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> = expectNoTaggedContentTransition(
        intermediatePropagationTolerance = intermediatePropagationTolerance,
        expectedUnaffectedTaggedContent = expectedUnaffectedTaggedElements.elementByTag,
    )

    fun <ElementT> expectNoTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUnaffectedTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> =
        object : AbstractExpectedReactiveBagContentTransition<ElementT>() {
            override val expectedOldTaggedContent: Map<Tag, ElementT> = expectedUnaffectedTaggedContent

            override val expectedNewTaggedContent: Map<Tag, ElementT> = expectedUnaffectedTaggedContent

            override val expectedReaction: ExpectedBasicReactiveBagChange<ElementT> =
                object : AbstractExpectedReactiveBagChange<ElementT>() {
                    override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
                        intermediatePropagationTolerance

                    override val expectedEffectiveChange: TaggedBagChange<ElementT>? = null
                }
        }

    fun <ElementT> expectStableTaggedContent(
        expectedTaggedElements: TaggedBag<ElementT>,
    ): ExpectedReactiveBagContent<ElementT> = expectStableTaggedContent(
        expectedTaggedContent = expectedTaggedElements.elementByTag,
    )

    fun <ElementT> expectStableTaggedContent(
        expectedTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContent<ElementT> = object : ExpectedReactiveBagContent<ElementT> {
        override fun verifyStableState(
            propagationContext: Transactions.PropagationContext,
            subject: ReactiveBag<ElementT>,
        ) {
            val actualTaggedContent = subject.trackedVertex.getOldContentView(
                processingContext = propagationContext,
            ).elementByTag

            assertEquals(
                expected = expectedTaggedContent,
                actual = actualTaggedContent,
                message = "The stable tagged content of the reactive bag did not match the expected stable tagged content.",
            )
        }
    }
}
