package dev.azide.core.impl.event_stream

import dev.azide.core.impl.ReactiveFinalizationRegistry
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionSubscriber
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionNotificationSubscriber
import dev.azide.core.impl.event_stream.EventStreamVertex.SubscriberStatus
import dev.azide.core.impl.utils.weak_bag.MutableBag
import dev.kmpx.platform.PlatformWeakReference
import kotlin.jvm.JvmInline

interface LiveEventStreamVertex<out EventT> : EventStreamVertex<EventT> {
    class WeaklyReferencedEmissionNotificationSubscriber<EventT>(
        private val sourceEventStreamVertex: EventStreamVertex<EventT>,
        emissionSubscriber: EmissionSubscriber<EventT>,
    ) : EmissionNotificationSubscriber<EventT> {
        private val basicSubscriberWeakReference = PlatformWeakReference(emissionSubscriber)

        override fun handleEmission(
            propagationContext: Transactions.PropagationContext,
        ): SubscriberStatus {
            when (val basicSubscriber = basicSubscriberWeakReference.get()) {
                null -> {
                    return SubscriberStatus.Unreachable
                }

                else -> {
                    basicSubscriber.handleEmission(
                        propagationContext = propagationContext,
                    )

                    return SubscriberStatus.Reachable
                }
            }
        }
    }

    @JvmInline
    value class LiveSubscriberHandle<EventT>(
        val internalHandle: MutableBag.Handle<EmissionNotificationSubscriber<EventT>>,
    ) : EventStreamVertex.SubscriberHandle

    interface WeakSubscriberHandle {
        fun cancel()
    }
}

fun <EventT> EmissionSubscriber<EventT>.weaklyReferenced(
    sourceEventStreamVertex: EventStreamVertex<EventT>,
): LiveEventStreamVertex.WeaklyReferencedEmissionNotificationSubscriber<EventT> = LiveEventStreamVertex.WeaklyReferencedEmissionNotificationSubscriber(
    sourceEventStreamVertex = sourceEventStreamVertex,
    emissionSubscriber = this,
)

/**
 * Register a [subscriber] related to a [dependentVertex]. When the [dependentVertex] object is garbage collected, the
 * subscriber will be unregistered. The subscriber will be registered indirectly (via a weakly-referencing wrapper), so
 * it may safely reference the [dependentVertex] object without creating a strong reference cycle.
 *
 * In a special (supported) case, [dependentVertex] and [subscriber] might be the same object.
 */
fun <EventT> EventStreamVertex<EventT>.registerEmissionSubscriberWeakly(
    propagationContext: Transactions.PropagationContext,
    dependentVertex: Vertex,
    subscriber: EmissionSubscriber<EventT>,
    mode: ActivationMode,
): LiveEventStreamVertex.WeakSubscriberHandle {
    val innerSubscriberHandle: EventStreamVertex.SubscriberHandle = registerEmissionNotificationSubscriber(
        propagationContext = propagationContext,
        subscriber = subscriber.weaklyReferenced(
            sourceEventStreamVertex = this@registerEmissionSubscriberWeakly,
        ),
        mode = mode,
    )

    /*
     * Register a cleanup transaction that unregisters the subscriber from the source vertex when the dependent vertex
     * is garbage collected.
     *
     * We know this is a correct operation, as the subscribers can't have any impact on the reactive system without
     * their related vertex.
     *
     * Each vertex cleans the unreachable subscribers on its own, but it does so only when it has something to propagate.
     * So most of the time, no significant amount of memory would leak if we didn't proactively unsubscribe as a part of
     * the observer's finalization.
     *
     * In a corner case scenario when the source event stream emits rarely (or never), but it continuously gets new
     * short-lived loose observers, the abandoned subscriber entries would constitute a significant memory leak.
     */
    val finalizationHandle: ReactiveFinalizationRegistry.Handle = ReactiveFinalizationRegistry.register(
        target = dependentVertex, finalizationCallback = {
            this@registerEmissionSubscriberWeakly.unregisterSubscriber(
                handle = innerSubscriberHandle,
            )
        })

    return object : LiveEventStreamVertex.WeakSubscriberHandle {
        override fun cancel() {
            // TODO: If vertex succession is implemented, then this vertex might not contain the given subscription, as
            //  would possibly be migrated!
            this@registerEmissionSubscriberWeakly.unregisterSubscriber(
                handle = innerSubscriberHandle,
            )

            finalizationHandle.unregister()
        }
    }
}
