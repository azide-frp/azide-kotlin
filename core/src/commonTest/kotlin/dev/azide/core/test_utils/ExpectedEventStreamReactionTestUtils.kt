package dev.azide.core.test_utils

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.registerObserverOnline
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex.BasicSubscriber
import dev.azide.core.impl.event_stream.registerSubscriberOnline
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

typealias ExpectedEventStreamReaction<EventT> = ExpectedTestSubjectReaction<EventStream<EventT>>

typealias ExpectedEventStreamTransition<EventT> = ExpectedTestSubjectTransition<EventStream<EventT>>

val <EventT> ExpectedEventStreamReaction<EventT>.asExpectedTransition: ExpectedEventStreamTransition<EventT>
    get() = ExpectedEventStreamTransition(
        expectedOldState = ExpectedTestSubjectState.None,
        expectedReaction = this,
        expectedNewState = ExpectedTestSubjectState.None,
    )

private abstract class AbstractExpectedEventStreamReaction<EventT> : ExpectedEventStreamReaction<EventT> {
    final override fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<EventStream<EventT>>,
    ): TestSubjectReactionVerifier = object : TestSubjectReactionVerifier, BasicSubscriber<EventT> {
        private val subjectVertex: EventStreamVertex<EventT>
            get() = subjectLazy.value.vertex

        private var subscriberHandle: EventStreamVertex.SubscriberHandle? = null

        private var initialEmission: EventStreamVertex.Emission<EventT>? = null

        private val receivedEmissions = mutableListOf<EventStreamVertex.Emission<EventT>?>()

        override fun install() {
            if (subscriberHandle != null) {
                throw IllegalStateException("Event stream verifier is already installed")
            }

            subscriberHandle = subjectVertex.registerSubscriberOnline(
                propagationContext = propagationContext,
                subscriber = this,
            )

            initialEmission = subjectVertex.ongoingEmission
        }

        override fun verifyReaction() {
            if (subscriberHandle == null) {
                throw IllegalStateException("A non-installed verifier cannot be used for verification")
            }

            assertEquals(
                expected = expectedEffectiveEmission,
                actual = subjectVertex.ongoingEmission,
                message = "Exposed ongoing emission did not match the expected emission.",
            )

            val effectiveEmission = when {
                receivedEmissions.isNotEmpty() -> receivedEmissions.last()
                else -> initialEmission
            }

            assertEquals(
                expected = expectedEffectiveEmission,
                actual = effectiveEmission,
                message = "The effective received emission did not match the expected emission.",
            )

            when (intermediatePropagationTolerance) {
                IntermediatePropagationTolerance.DoNotTolerate -> {
                    assertTrue(
                        actual = receivedEmissions.size <= 1,
                        message = "Expected at most one emission to be propagated, but received ${receivedEmissions.size} emissions (intermediate propagation is not tolerated).",
                    )
                }

                IntermediatePropagationTolerance.Tolerate -> {}
            }
        }

        override fun uninstall() {
            val subscriberHandle = this.subscriberHandle
                ?: throw IllegalStateException("Cannot uninstall a non-installed event stream verifier")

            subjectVertex.unregisterSubscriber(
                handle = subscriberHandle,
            )

            this.subscriberHandle = null
            this.initialEmission = null
        }

        override fun handleEmission(
            propagationContext: Transactions.PropagationContext,
            emission: EventStreamVertex.Emission<EventT>?,
        ) {
            receivedEmissions.add(emission)
        }
    }

    abstract val intermediatePropagationTolerance: IntermediatePropagationTolerance

    abstract val expectedEffectiveEmission: EventStreamVertex.Emission<EventT>?
}

object ExpectedEventStreamReactionTestUtils {
    fun <EventT> expectEmission(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedEmittedEvent: EventT,
    ): ExpectedEventStreamTransition<EventT> = object : AbstractExpectedEventStreamReaction<EventT>() {
        override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
            intermediatePropagationTolerance

        override val expectedEffectiveEmission: EventStreamVertex.Emission<EventT> = EventStreamVertex.Emission(
            emittedEvent = expectedEmittedEvent,
        )
    }.asExpectedTransition


    fun <EventT> expectNoEmission(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedEventStreamTransition<EventT> = object : AbstractExpectedEventStreamReaction<EventT>() {
        override val expectedEffectiveEmission: EventStreamVertex.Emission<EventT>? = null

        override val intermediatePropagationTolerance = intermediatePropagationTolerance
    }.asExpectedTransition
}
