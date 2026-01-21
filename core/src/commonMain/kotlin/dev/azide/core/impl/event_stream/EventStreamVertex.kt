package dev.azide.core.impl.event_stream

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionSubscriber
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionNotificationSubscriber
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

    interface EmissionNotificationSubscriber {
        object Noop : EmissionNotificationSubscriber {
            override fun handleEmission(
                propagationContext: Transactions.PropagationContext,
            ): SubscriberStatus = SubscriberStatus.Reachable
        }

        fun handleEmission(
            propagationContext: Transactions.PropagationContext,
        ): SubscriberStatus
    }

    interface EmissionSubscriber {
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

    fun registerEmissionNotificationSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: EmissionNotificationSubscriber,
        mode: ActivationMode,
    ): SubscriberHandle

    fun unregisterSubscriber(
        handle: SubscriberHandle,
    )
}

fun <EventT> EventStreamVertex<EventT>.registerEmissionNotificationSubscriberOnline(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionNotificationSubscriber,
): SubscriberHandle = registerEmissionNotificationSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerEmissionSubscriber(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionSubscriber,
    mode: ActivationMode,
): SubscriberHandle = registerEmissionNotificationSubscriber(
    propagationContext = propagationContext,
    subscriber = object : EmissionNotificationSubscriber {
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

fun <EventT> EventStreamVertex<EventT>.registerEmissionSubscriberOnline(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionSubscriber,
): SubscriberHandle = registerEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerEmissionSubscriberOffline(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionSubscriber,
): SubscriberHandle = registerEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Offline,
)
