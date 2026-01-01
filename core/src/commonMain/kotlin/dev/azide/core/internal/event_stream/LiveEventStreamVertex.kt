package dev.azide.core.internal.event_stream

import dev.azide.core.internal.ReactiveFinalizationRegistry
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.Vertex
import dev.azide.core.internal.event_stream.EventStreamVertex.Subscriber
import dev.azide.core.internal.event_stream.EventStreamVertex.SubscriberStatus
import dev.azide.core.internal.utils.weak_bag.MutableBag
import dev.kmpx.platform.PlatformWeakReference
import kotlin.jvm.JvmInline

interface LiveEventStreamVertex<out EventT> : EventStreamVertex<EventT> {
    interface BasicSubscriber<in EventT> : Subscriber<EventT> {
        override fun handleEmissionWithStatus(
            propagationContext: Transactions.PropagationContext,
            emission: EventStreamVertex.Emission<EventT>?,
        ): SubscriberStatus {
            handleEmission(
                propagationContext = propagationContext,
                emission = emission,
            )

            return SubscriberStatus.Reachable
        }

        fun handleEmission(
            propagationContext: Transactions.PropagationContext,
            emission: EventStreamVertex.Emission<EventT>?,
        )
    }

    class WeaklyReferencedSubscriber<EventT>(
        basicSubscriber: BasicSubscriber<EventT>,
    ) : Subscriber<EventT> {
        private val basicSubscriberWeakReference = PlatformWeakReference(basicSubscriber)

        override fun handleEmissionWithStatus(
            propagationContext: Transactions.PropagationContext,
            emission: EventStreamVertex.Emission<EventT>?,
        ): SubscriberStatus {
            when (val basicSubscriber = basicSubscriberWeakReference.get()) {
                null -> {
                    return SubscriberStatus.Unreachable
                }

                else -> {
                    basicSubscriber.handleEmission(
                        propagationContext = propagationContext,
                        emission = emission,
                    )

                    return SubscriberStatus.Reachable
                }
            }
        }
    }

    @JvmInline
    value class LiveSubscriberHandle<EventT>(
        val internalHandle: MutableBag.Handle<Subscriber<EventT>>,
    ) : EventStreamVertex.SubscriberHandle

    interface WeakSubscriberHandle {
        fun cancel()
    }
}

fun <EventT> LiveEventStreamVertex.BasicSubscriber<EventT>.weaklyReferenced(): LiveEventStreamVertex.WeaklyReferencedSubscriber<EventT> =
    LiveEventStreamVertex.WeaklyReferencedSubscriber(
        basicSubscriber = this,
    )

/**
 * Register a [subscriber] related to a [dependentVertex]. When the [dependentVertex] object is garbage collected, the
 * subscriber will be unregistered. The subscriber will be registered indirectly (via a weakly-referencing wrapper), so
 * it may safely reference the [dependentVertex] object without creating a strong reference cycle.
 *
 * In a special (supported) case, [dependentVertex] and [subscriber] might be the same object.
 */
fun <EventT> EventStreamVertex<EventT>.registerSubscriberWeakly(
    propagationContext: Transactions.PropagationContext,
    dependentVertex: Vertex,
    subscriber: LiveEventStreamVertex.BasicSubscriber<EventT>,
): LiveEventStreamVertex.WeakSubscriberHandle {
    val innerSubscriberHandle: EventStreamVertex.SubscriberHandle = registerSubscriber(
        propagationContext = propagationContext,
        subscriber = subscriber.weaklyReferenced(),
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
        target = dependentVertex,
        finalizationCallback = {
            this@registerSubscriberWeakly.unregisterSubscriber(
                handle = innerSubscriberHandle,
            )
        }
    )

    return object : LiveEventStreamVertex.WeakSubscriberHandle {
        override fun cancel() {
            // TODO: If vertex succession is implemented, then this vertex might not contain the given subscription, as
            //  would possibly be migrated!
            this@registerSubscriberWeakly.unregisterSubscriber(
                handle = innerSubscriberHandle,
            )

            finalizationHandle.unregister()
        }
    }
}
