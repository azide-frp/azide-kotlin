package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.MomentContext
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.event_stream.EventStreamVertex.Emission
import dev.azide.core.impl.Vertex.Listener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.TerminatedEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListenerOnline
import dev.azide.core.impl.event_stream.registerListenerOnline
import dev.azide.core.pullInternallyWrappedUp
import dev.azide.core.test_utils.TestStimulation
import kotlin.jvm.JvmInline
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal object EventStreamTestUtils {
    fun <EventT> createInputEventStream(): TestInputEventStream<EventT> = TestInputEventStream()

    /**
     * Spawn a stateful event stream, not expecting it to emit during spawn.
     */
    fun <EventT> spawnStatefulEventStream(
        inputStimulation: TestStimulation? = null,
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
        inputStimulation: TestStimulation? = null,
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

        // Register a listener, as event stream vertices aren't required to expose the emission otherwise
        subjectVertex.registerListenerOnline(
            propagationContext = propagationContext,
            listener = Listener.Noop,
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
        inputStimulation: TestStimulation? = null,
        expectedEmittedEvent: EventT,
        spawn: context(MomentContext) () -> EventStream<EventT>,
    ): EventStream<EventT> = spawnStatefulEventStreamExpectingEmission(
        inputStimulation = inputStimulation,
        expectedEmittedEvent = expectedEmittedEvent,
        spawn = Moment.decontextualize(spawn),
    )

    class SubscribingVerifier<EventT>(
        private val subjectVertex: LiveEventStreamVertex<EventT>,
    ) : BoundListener {
        @JvmInline
        private value class ReceivedEmission<EventT>(
            val receivedEmission: Emission<EventT>?,
        )

        /**
         * The emission most recently received from the subject event stream. If no emission notification was received,
         * this is `null` (not to be confused with an emission notification carrying `null` emission).
         */
        private var receivedEmission: ReceivedEmission<EventT>? = null

        private var upstreamListenerHandle: ListenerHandle? =
            Transactions.executeWithResult { propagationContext ->
                subjectVertex.registerBoundListenerOnline(
                    propagationContext = propagationContext,
                    listener = this,
                )
            }

        /**
         * Verify that, under the given [inputStimulation], the subject event stream emits [expectedEmittedEvent].
         */
        fun verifyEmitsAsExpected(
            inputStimulation: TestStimulation,
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
            inputStimulation: TestStimulation,
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
            inputStimulation: TestStimulation,
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
            val upstreamListenerHandle =
                this.upstreamListenerHandle ?: throw IllegalStateException("Verifier is already stopped")

            subjectVertex.unregisterListener(
                handle = upstreamListenerHandle,
            )

            this.upstreamListenerHandle = null
        }

        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ) {
            receivedEmission = ReceivedEmission(
                receivedEmission = subjectVertex.ongoingEmission,
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
        inputStimulation: TestStimulation,
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
        inputStimulation: TestStimulation,
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
        inputStimulation: TestStimulation,
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
     * Register a no-op listener on the [subjectEventStream].
     */
    fun <EventT> registerNoopListener(
        subjectEventStream: EventStream<EventT>,
    ) {
        val subjectVertex = subjectEventStream.vertex as? LiveEventStreamVertex<EventT>
            ?: throw IllegalStateException("Subject cell vertex is already frozen")

        Transactions.execute { propagationContext ->
            subjectVertex.registerListenerOnline(
                propagationContext = propagationContext,
                listener = Listener.Noop,
            )
        }
    }
}
