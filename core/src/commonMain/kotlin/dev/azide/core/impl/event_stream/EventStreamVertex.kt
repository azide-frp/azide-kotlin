package dev.azide.core.impl.event_stream

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex.BoundEmissionSubscriber
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionSubscriber
import dev.azide.core.impl.event_stream.EventStreamVertex.SubscriberHandle
import kotlin.jvm.JvmInline

sealed interface EventStreamVertex<out EventT> : Vertex {
    @JvmInline
    value class Emission<out EventT>(
        val emittedEvent: EventT,
    ) {
        fun <TransformedEventT> map(
            transform: (EventT) -> TransformedEventT,
        ): Emission<TransformedEventT> = Emission(
            emittedEvent = transform(emittedEvent),
        )
    }

    interface EmissionSubscriber {
        object Noop : EmissionSubscriber {
            override fun handleEmission(
                propagationContext: Transactions.PropagationContext,
            ): SubscriberStatus = SubscriberStatus.Reachable
        }

        fun handleEmission(
            propagationContext: Transactions.PropagationContext,
        ): SubscriberStatus
    }

    interface BoundEmissionSubscriber {
        fun handleEmission(
            propagationContext: Transactions.PropagationContext,
        )
    }

    interface SubscriberHandle

    enum class SubscriberStatus {
        Reachable, Unreachable,
    }

    val ongoingEmission: Emission<EventT>?

    val subscriberCount: Int

    fun registerEmissionSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: EmissionSubscriber,
        mode: ActivationMode,
    ): SubscriberHandle

    fun unregisterSubscriber(
        handle: SubscriberHandle,
    )
}

fun <EventT> EventStreamVertex<EventT>.registerEmissionNotificationSubscriberOnline(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionSubscriber,
): SubscriberHandle = registerEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundEmissionSubscriber(
    propagationContext: Transactions.PropagationContext,
    subscriber: BoundEmissionSubscriber,
    mode: ActivationMode,
): SubscriberHandle = registerEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = object : EmissionSubscriber {
        override fun handleEmission(
            propagationContext: Transactions.PropagationContext,
        ): EventStreamVertex.SubscriberStatus {
            subscriber.handleEmission(
                propagationContext = propagationContext,
            )

            return EventStreamVertex.SubscriberStatus.Reachable
        }
    },
    mode = mode,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundEmissionSubscriberOnline(
    propagationContext: Transactions.PropagationContext,
    subscriber: BoundEmissionSubscriber,
): SubscriberHandle = registerBoundEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundEmissionSubscriberOffline(
    propagationContext: Transactions.PropagationContext,
    subscriber: BoundEmissionSubscriber,
): SubscriberHandle = registerBoundEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Offline,
)
