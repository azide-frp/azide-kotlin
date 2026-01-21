package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.CausalLoopException
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionNotificationSubscriber
import dev.azide.core.impl.event_stream.EventStreamVertex.SubscriberStatus
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex.LiveSubscriberHandle
import dev.azide.core.impl.utils.weak_bag.MutableBag

abstract class AbstractLiveEventStreamVertex<EventT> : LiveEventStreamVertex<EventT>, CommittableVertex {
    private val _registeredSubscribers: MutableBag<EmissionNotificationSubscriber> = MutableBag()

    override val subscriberCount: Int
        get() = _registeredSubscribers.size

    private var _ongoingEmission: EventStreamVertex.Emission<EventT>? = null

    private var _isPropagatingEmissionNotification = false

    private var _isEnqueuedForCommitment = false

    final override val ongoingEmission: EventStreamVertex.Emission<EventT>?
        get() = _ongoingEmission

    override fun registerEmissionNotificationSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: EmissionNotificationSubscriber,
        mode: Vertex.ActivationMode,
    ): EventStreamVertex.SubscriberHandle {
        val internalHandle = _registeredSubscribers.add(subscriber)

        if (_registeredSubscribers.size == 1) {
            onFirstSubscriberRegistered(
                propagationContext = propagationContext,
                mode = mode,
            )
        }

        return LiveSubscriberHandle(
            internalHandle = internalHandle,
        )
    }

    override fun unregisterSubscriber(
        handle: EventStreamVertex.SubscriberHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? LiveSubscriberHandle ?: throw IllegalArgumentException("Invalid handle")

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

    protected val hasSubscribers: Boolean
        get() = _registeredSubscribers.size > 0

    protected fun exposeAndPropagateEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<EventT>?,
    ) {
        exposeEmission(
            propagationContext = propagationContext,
            emission = emission,
        )

        propagateEmissionNotification(
            propagationContext = propagationContext,
        )
    }

    protected fun exposeEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<EventT>?,
    ) {
        _ongoingEmission = emission

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }

    protected fun ensureEnqueuedForCommitment(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (!_isEnqueuedForCommitment) {
            propagationContext.enqueueForCommitment(this)

            _isEnqueuedForCommitment = true
        }
    }

    protected fun clearExposedEmission() {
        _ongoingEmission = null
    }

    private fun propagateEmissionNotification(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (_isPropagatingEmissionNotification) {
            throw CausalLoopException("Causal loop detected in event stream vertex: $this")
        }

        try {
            _isPropagatingEmissionNotification = true

            _registeredSubscribers.forEach { subscriber ->
                val subscriberStatus = subscriber.handleEmission(
                    propagationContext = propagationContext,
                )

                // Remove the subscriber if it's unreachable
                subscriberStatus == SubscriberStatus.Unreachable
            }
        } finally {
            _isPropagatingEmissionNotification = false
        }
    }

    protected open fun onFirstSubscriberRegistered(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ) {
    }

    protected open fun onLastSubscriberUnregistered() {
    }

    protected open fun transit() {
    }
}
