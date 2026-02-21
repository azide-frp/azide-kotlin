package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.CausalLoopException
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.ListenableVertex.Listener
import dev.azide.core.impl.ListenableVertex.ListenerStatus
import dev.azide.core.impl.enqueueForCommitment
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex.LiveListenerHandle
import dev.azide.core.impl.utils.weak_bag.MutableBag

abstract class AbstractLiveEventStreamVertex<EventT> : LiveEventStreamVertex<EventT>, CommittableVertex {
    private val _registeredListeners: MutableBag<Listener> = MutableBag()

    override val listenerCount: Int
        get() = _registeredListeners.size

    private var _ongoingEmission: EventStreamVertex.Emission<EventT>? = null

    private var _isNotifyingListeners = false

    private var _isEnqueuedForCommitment = false

    final override val ongoingEmission: EventStreamVertex.Emission<EventT>?
        get() = _ongoingEmission

    override fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: Listener,
        mode: ListenableVertex.ActivationMode,
    ): ListenableVertex.ListenerHandle {
        val internalHandle = _registeredListeners.add(listener)

        if (_registeredListeners.size == 1) {
            onFirstListenerRegistered(
                propagationContext = propagationContext,
                mode = mode,
            )
        }

        return LiveListenerHandle(
            internalHandle = internalHandle,
        )
    }

    override fun unregisterListener(
        handle: ListenableVertex.ListenerHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? LiveListenerHandle ?: throw IllegalArgumentException("Invalid handle")

        _registeredListeners.remove(handleImpl.internalHandle)

        if (_registeredListeners.size == 0) {
            onLastListenerUnregistered()
        }
    }

    final override fun commit() {
        if (_ongoingEmission != null) {
            transit()
        }

        _ongoingEmission = null
        _isEnqueuedForCommitment = false
    }

    protected val hasListeners: Boolean
        get() = _registeredListeners.size > 0

    protected fun exposeEmissionNotifyingListeners(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<EventT>?,
    ) {
        exposeEmission(
            propagationContext = propagationContext,
            emission = emission,
        )

        notifyListeners(
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

    private fun notifyListeners(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (_isNotifyingListeners) {
            throw CausalLoopException("Causal loop detected in event stream vertex: $this")
        }

        try {
            _isNotifyingListeners = true

            _registeredListeners.forEach { listener ->
                val listenerStatus = listener.handle(
                    propagationContext = propagationContext,
                )

                // Remove the listener if it's unreachable
                listenerStatus == ListenerStatus.Unreachable
            }
        } finally {
            _isNotifyingListeners = false
        }
    }

    protected open fun onFirstListenerRegistered(
        propagationContext: Transactions.PropagationContext,
        mode: ListenableVertex.ActivationMode,
    ) {
    }

    protected open fun onLastListenerUnregistered() {
    }

    protected open fun transit() {
    }
}
