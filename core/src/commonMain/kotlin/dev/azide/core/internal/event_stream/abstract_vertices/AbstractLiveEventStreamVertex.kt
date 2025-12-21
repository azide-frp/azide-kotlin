package dev.azide.core.internal.event_stream.abstract_vertices

import dev.azide.core.internal.CommittableVertex
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.utils.weak_bag.MutableBag
import kotlin.jvm.JvmInline

abstract class AbstractLiveEventStreamVertex<EventT> : LiveEventStreamVertex<EventT>, CommittableVertex {
    @JvmInline
    private value class SubscriberHandleImpl<EventT>(
        val internalHandle: MutableBag.Handle<LiveEventStreamVertex.Subscriber<EventT>>,
    ) : LiveEventStreamVertex.SubscriberHandle

    private val _registeredSubscribers: MutableBag<LiveEventStreamVertex.Subscriber<EventT>> = MutableBag()

    private var _ongoingEmission: EventStreamVertex.Emission<EventT>? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingEmission: EventStreamVertex.Emission<EventT>?
        get() = _ongoingEmission

    override fun registerSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: LiveEventStreamVertex.Subscriber<EventT>,
    ): LiveEventStreamVertex.SubscriberHandle {
        val internalHandle = _registeredSubscribers.add(subscriber)

        if (_registeredSubscribers.size == 1) {
            onFirstSubscriberRegistered(
                propagationContext = propagationContext,
            )
        }

        return SubscriberHandleImpl(
            internalHandle = internalHandle,
        )
    }

    override fun unregisterSubscriber(
        handle: LiveEventStreamVertex.SubscriberHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? SubscriberHandleImpl<EventT> ?: throw IllegalArgumentException("Invalid handle")

        _registeredSubscribers.remove(handleImpl.internalHandle)

        if (_registeredSubscribers.size == 0) {
            onLastSubscriberUnregistered()
        }
    }

    final override fun commit() {
        if (_ongoingEmission != null) {
            transit()
        }

        _ongoingEmission = null
        _isEnqueuedForCommitment = false
    }

    protected fun exposeAndPropagateEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<EventT>?,
    ) {
        exposeEmission(
            propagationContext = propagationContext,
            emission = emission,
        )

        propagateEmission(
            propagationContext = propagationContext,
            emission = emission,
        )
    }

    protected fun exposeEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<EventT>?,
    ) {
        _ongoingEmission = emission

        if (!_isEnqueuedForCommitment) {
            propagationContext.enqueueForCommitment(this)

            _isEnqueuedForCommitment = true
        }
    }

    protected fun clearExposedEmission() {
        _ongoingEmission = null
    }

    private fun propagateEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<EventT>?,
    ) {
        _registeredSubscribers.forEach { subscriber ->
            val subscriberStatus = subscriber.handleEmissionWithStatus(
                propagationContext = propagationContext,
                emission = emission,
            )

            // Remove the subscriber if it's unreachable
            subscriberStatus == LiveEventStreamVertex.SubscriberStatus.Unreachable
        }
    }

    protected open fun onFirstSubscriberRegistered(
        propagationContext: Transactions.PropagationContext,
    ) {
    }

    protected open fun onLastSubscriberUnregistered() {
    }

    protected open fun transit() {
    }
}
