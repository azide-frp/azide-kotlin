package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.MomentContext
import dev.azide.core.MomentContextImpl
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.EventStreamVertex.Emission
import dev.azide.core.internal.event_stream.EventStreamVertex.Subscriber
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex.BasicSubscriber
import dev.azide.core.internal.event_stream.TerminatedEventStreamVertex
import dev.azide.core.pullInternallyWrappedUp
import dev.azide.core.test_utils.TestInputStimulation
import kotlin.jvm.JvmInline
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal object EventStreamTestUtils {
    fun <EventT> createInputEventStream(): dev.azide.core.test_utils.event_stream.TestInputEventStream<EventT> =
        TestInputEventStream()

    /**
     * Spawn a stateful event stream, not expecting it to emit during spawn.
     */
    fun <EventT> spawnStatefulEventStream(
        inputStimulation: TestInputStimulation? = null,
        spawn: context(MomentContext) () -> EventStream<EventT>,
    ): EventStream<EventT> = Transactions.executeWithResult { propagationContext ->
        inputStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        val subjectEventStream = Moment.decontextualize(spawn).pullInternallyWrappedUp(
            propagationContext = propagationContext,
        )

        val ongoingEmission = subjectEventStream.vertex.ongoingEmission

        assertNull(
            actual = ongoingEmission,
            message = "Spawned subject event stream has an ongoing emission unexpectedly",
        )

        return@executeWithResult subjectEventStream
    }

    /**
     * Spawn a stateful event stream, expecting it to emit during spawn with [expectedEmittedEvent].
     */
    fun <EventT> spawnStatefulEventStreamExpectingEmission(
        inputStimulation: TestInputStimulation? = null,
        expectedEmittedEvent: EventT,
        spawn: Moment<EventStream<EventT>>,
    ): EventStream<EventT> = Transactions.executeWithResult { propagationContext ->
        inputStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        val subjectEventStream = spawn.pullInternallyWrappedUp(
            propagationContext = propagationContext,
        )

        val subjectVertex = subjectEventStream.vertex

        // Register a subscriber, as event stream vertices aren't required to expose the emission otherwise
        subjectVertex.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = Subscriber.Noop,
        )

        val ongoingEmission = subjectVertex.ongoingEmission

        assertNotNull(
            actual = ongoingEmission,
            message = "Spawned subject event stream has no ongoing emission unexpectedly",
        )

        assertEquals(
            expected = expectedEmittedEvent,
            actual = ongoingEmission.emittedEvent,
            message = "Spawned subject event stream's emitted event did not match expected event",
        )

        return@executeWithResult subjectEventStream
    }

    /**
     * Spawn a stateful event stream, expecting it to emit during spawn with [expectedEmittedEvent].
     */
    fun <EventT> spawnStatefulEventStreamExpectingEmission(
        inputStimulation: TestInputStimulation? = null,
        expectedEmittedEvent: EventT,
        spawn: context(MomentContext) () -> EventStream<EventT>,
    ): EventStream<EventT> = spawnStatefulEventStreamExpectingEmission(
        inputStimulation = inputStimulation,
        expectedEmittedEvent = expectedEmittedEvent,
        spawn = Moment.decontextualize(spawn),
    )

    class SubscribingVerifier<EventT>(
        private val subjectVertex: LiveEventStreamVertex<EventT>,
    ) : BasicSubscriber<EventT> {
        @JvmInline
        private value class ReceivedEmission<EventT>(
            val receivedEmission: Emission<EventT>?,
        )

        /**
         * The emission most recently received from the subject event stream. If no emission notification was received,
         * this is `null` (not to be confused with an emission notification carrying `null` emission).
         */
        private var receivedEmission: ReceivedEmission<EventT>? = null

        private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? =
            Transactions.executeWithResult { propagationContext ->
                subjectVertex.registerSubscriber(
                    propagationContext = propagationContext,
                    subscriber = this,
                )
            }

        /**
         * Verify that, under the given [inputStimulation], the subject event stream emits [expectedEmittedEvent].
         */
        fun verifyEmitsAsExpected(
            inputStimulation: TestInputStimulation,
            expectedEmittedEvent: EventT,
        ) {
            // Clear the emission potentially received in separate transactions
            receivedEmission = null

            Transactions.execute { propagationContext ->
                inputStimulation.stimulate(
                    propagationContext = propagationContext,
                )
            }

            assertEquals(
                expected = ReceivedEmission(
                    receivedEmission = Emission(
                        emittedEvent = expectedEmittedEvent,
                    ),
                ),
                actual = receivedEmission,
                message = "Received emission mismatch",
            )

            // Clear the emission, as it's not needed after the verification
            receivedEmission = null
        }


        /**
         * Verify that, in spite of the given [inputStimulation], the subject event stream does not emit any event. This
         * utility is meant for verifying complete silence. If even a single emission notification is propagated by the
         * subject event stream's vertex during the transaction (even if it's later corrected), the verification will
         * fail.
         */
        fun verifyDoesNotEmitAtAll(
            inputStimulation: TestInputStimulation,
        ) {
            // Clear the emission potentially received in separate transactions
            receivedEmission = null

            Transactions.execute { propagationContext ->
                inputStimulation.stimulate(
                    propagationContext = propagationContext,
                )
            }

            assertNull(
                actual = receivedEmission,
                message = "Subject event stream emitted unexpectedly",
            )
        }

        /**
         * Verify that, in spite of the given [inputStimulation], the subject event stream does not effectively emit any
         * event. This utility is meant for testing emission revoking. If not even a single emission notification (later
         * revoked) is propagated by the subject event stream's vertex during the transaction, the verification will fail.
         */
        fun verifyDoesNotEmitEffectively(
            inputStimulation: TestInputStimulation,
        ) {
            // Clear the emission potentially received in separate transactions
            receivedEmission = null

            Transactions.execute { propagationContext ->
                inputStimulation.stimulate(
                    propagationContext = propagationContext,
                )
            }

            assertEquals(
                expected = ReceivedEmission(
                    receivedEmission = null,
                ),
                actual = receivedEmission,
                message = "Subject event stream emitted unexpectedly",
            )
        }

        fun stop() {
            val upstreamSubscriberHandle =
                this.upstreamSubscriberHandle ?: throw IllegalStateException("Verifier is already stopped")

            subjectVertex.unregisterSubscriber(
                handle = upstreamSubscriberHandle,
            )

            this.upstreamSubscriberHandle = null
        }

        override fun handleEmission(
            propagationContext: Transactions.PropagationContext,
            emission: Emission<EventT>?,
        ) {
            receivedEmission = ReceivedEmission(
                receivedEmission = emission,
            )
        }
    }

    fun <EventT> subscribeForVerification(
        subjectEventStream: EventStream<EventT>,
    ): SubscribingVerifier<EventT> {
        val subjectVertex = subjectEventStream.vertex as? LiveEventStreamVertex<EventT>
            ?: throw IllegalStateException("Subject event stream vertex is already terminated")

        return SubscribingVerifier(
            subjectVertex = subjectVertex,
        )
    }

    /**
     * Verify that the [subjectEventStream] is terminated (won't ever emit).
     */
    fun <EventT> verifyTerminated(
        subjectEventStream: EventStream<EventT>,
    ) {
        val subjectVertex = subjectEventStream.vertex

        assertIs<TerminatedEventStreamVertex<EventT>>(
            value = subjectVertex,
            message = "Subject event stream is not terminated as expected",
        )
    }

    /**
     * A helper wrapper for [SubscribingVerifier.verifyEmitsAsExpected], which subscribes to the  [subjectEventStream]
     * for the purpose of a single emission verification.
     */
    fun <EventT> verifyEmitsAsExpected(
        subjectEventStream: EventStream<EventT>,
        inputStimulation: TestInputStimulation,
        expectedEmittedEvent: EventT,
    ) {
        val subscribingVerifier = subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        subscribingVerifier.verifyEmitsAsExpected(
            inputStimulation = inputStimulation,
            expectedEmittedEvent = expectedEmittedEvent,
        )

        subscribingVerifier.stop()
    }

    /**
     * A helper wrapper for [SubscribingVerifier.verifyDoesNotEmitAtAll], which subscribes to the [subjectEventStream]
     * for the purpose of a single emission verification.
     */
    fun <EventT> verifyDoesNotEmitAtAll(
        subjectEventStream: EventStream<EventT>,
        inputStimulation: TestInputStimulation,
    ) {
        val subscribingVerifier = subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        subscribingVerifier.verifyDoesNotEmitAtAll(
            inputStimulation = inputStimulation,
        )

        subscribingVerifier.stop()
    }

    /**
     * A helper wrapper for [SubscribingVerifier.verifyDoesNotEmitEffectively], which subscribes to the
     * [subjectEventStream] for the purpose of a single emission verification.
     */
    fun <EventT> verifyDoesNotEmitEffectively(
        subjectEventStream: EventStream<EventT>,
        inputStimulation: TestInputStimulation,
    ) {
        val subscribingVerifier = subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        subscribingVerifier.verifyDoesNotEmitEffectively(
            inputStimulation = inputStimulation,
        )

        subscribingVerifier.stop()
    }

    /**
     * Register a no-op subscriber on the [subjectEventStream].
     */
    fun <EventT> registerNoopSubscriber(
        subjectEventStream: EventStream<EventT>,
    ) {
        val subjectVertex = subjectEventStream.vertex as? LiveEventStreamVertex<EventT>
            ?: throw IllegalStateException("Subject cell vertex is already frozen")

        Transactions.execute { propagationContext ->
            subjectVertex.registerSubscriber(
                propagationContext = propagationContext,
                subscriber = Subscriber.Noop,
            )
        }
    }
}
