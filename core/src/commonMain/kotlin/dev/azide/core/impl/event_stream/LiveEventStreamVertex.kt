package dev.azide.core.impl.event_stream

import dev.azide.core.impl.ReactiveFinalizationRegistry
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex.BoundListener
import dev.azide.core.impl.event_stream.EventStreamVertex.Listener
import dev.azide.core.impl.event_stream.EventStreamVertex.ListenerStatus
import dev.azide.core.impl.utils.weak_bag.MutableBag
import dev.kmpx.platform.PlatformWeakReference
import kotlin.jvm.JvmInline

interface LiveEventStreamVertex<out EventT> : EventStreamVertex<EventT> {
    class WeaklyReferencedListener<EventT>(
        private val sourceEventStreamVertex: EventStreamVertex<EventT>,
        emissionListener: BoundListener,
    ) : Listener {
        private val basicListenerWeakReference = PlatformWeakReference(emissionListener)

        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ): ListenerStatus {
            when (val basicListener = basicListenerWeakReference.get()) {
                null -> {
                    return ListenerStatus.Unreachable
                }

                else -> {
                    basicListener.handleEmission(
                        propagationContext = propagationContext,
                    )

                    return ListenerStatus.Reachable
                }
            }
        }
    }

    @JvmInline
    value class LiveListenerHandle(
        val internalHandle: MutableBag.Handle<Listener>,
    ) : EventStreamVertex.ListenerHandle

    interface WeakListenerHandle {
        fun cancel()
    }
}

fun <EventT> BoundListener.weaklyReferenced(
    sourceEventStreamVertex: EventStreamVertex<EventT>,
): LiveEventStreamVertex.WeaklyReferencedListener<EventT> = LiveEventStreamVertex.WeaklyReferencedListener(
    sourceEventStreamVertex = sourceEventStreamVertex,
    emissionListener = this,
)

/**
 * Register a [listener] related to a [dependentVertex]. When the [dependentVertex] object is garbage collected, the
 * listener will be unregistered. The listener will be registered indirectly (via a weakly-referencing wrapper), so
 * it may safely reference the [dependentVertex] object without creating a strong reference cycle.
 *
 * In a special (supported) case, [dependentVertex] and [listener] might be the same object.
 */
fun <EventT> EventStreamVertex<EventT>.registerEmissionListenerWeakly(
    propagationContext: Transactions.PropagationContext,
    dependentVertex: Vertex,
    listener: BoundListener,
    mode: ActivationMode,
): LiveEventStreamVertex.WeakListenerHandle {
    val innerListenerHandle: EventStreamVertex.ListenerHandle = registerListener(
        propagationContext = propagationContext,
        listener = listener.weaklyReferenced(
            sourceEventStreamVertex = this@registerEmissionListenerWeakly,
        ),
        mode = mode,
    )

    /*
     * Register a cleanup transaction that unregisters the listener from the source vertex when the dependent vertex
     * is garbage collected.
     *
     * We know this is a correct operation, as the listeners can't have any impact on the reactive system without
     * their related vertex.
     *
     * Each vertex cleans the unreachable listeners on its own, but it does so only when it has something to propagate.
     * So most of the time, no significant amount of memory would leak if we didn't proactively unsubscribe as a part of
     * the observer's finalization.
     *
     * In a corner case scenario when the source event stream emits rarely (or never), but it continuously gets new
     * short-lived loose observers, the abandoned listener entries would constitute a significant memory leak.
     */
    val finalizationHandle: ReactiveFinalizationRegistry.Handle = ReactiveFinalizationRegistry.register(
        target = dependentVertex, finalizationCallback = {
            this@registerEmissionListenerWeakly.unregisterListener(
                handle = innerListenerHandle,
            )
        })

    return object : LiveEventStreamVertex.WeakListenerHandle {
        override fun cancel() {
            // TODO: If vertex succession is implemented, then this vertex might not contain the given subscription, as
            //  would possibly be migrated!
            this@registerEmissionListenerWeakly.unregisterListener(
                handle = innerListenerHandle,
            )

            finalizationHandle.unregister()
        }
    }
}
