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

    interface EmissionNotificationSubscriber<in EventT> {
        object Noop : EmissionNotificationSubscriber<Any?> {
            override fun handleEmissionNotification(
                propagationContext: Transactions.PropagationContext,
            ): SubscriberStatus = SubscriberStatus.Reachable
        }

        fun handleEmissionNotification(
            propagationContext: Transactions.PropagationContext,
        ): SubscriberStatus
    }

    interface EmissionSubscriber<in EventT> {
        fun handleEmission(
            propagationContext: Transactions.PropagationContext,
            emission: Emission<EventT>?,
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
        subscriber: EmissionNotificationSubscriber<EventT>,
        mode: ActivationMode,
    ): SubscriberHandle

    fun unregisterSubscriber(
        handle: SubscriberHandle,
    )
}

fun <EventT> EventStreamVertex<EventT>.registerEmissionNotificationSubscriberOnline(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionNotificationSubscriber<EventT>,
): SubscriberHandle = registerEmissionNotificationSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerEmissionSubscriber(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionSubscriber<EventT>,
    mode: ActivationMode,
): SubscriberHandle = registerEmissionNotificationSubscriber(
    propagationContext = propagationContext,
    subscriber = object : EmissionNotificationSubscriber<EventT> {
        override fun handleEmissionNotification(
            propagationContext: Transactions.PropagationContext,
        ): EventStreamVertex.SubscriberStatus {
            subscriber.handleEmission(
                propagationContext = propagationContext,
                emission = this@registerEmissionSubscriber.ongoingEmission,
            )

            return EventStreamVertex.SubscriberStatus.Reachable
        }
    },
    mode = mode,
)

fun <EventT> EventStreamVertex<EventT>.registerEmissionSubscriberOnline(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionSubscriber<EventT>,
): SubscriberHandle = registerEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerEmissionSubscriberOffline(
    propagationContext: Transactions.PropagationContext,
    subscriber: EmissionSubscriber<EventT>,
): SubscriberHandle = registerEmissionSubscriber(
    propagationContext = propagationContext,
    subscriber = subscriber,
    mode = ActivationMode.Offline,
)
