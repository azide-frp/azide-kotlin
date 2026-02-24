package dev.azide.core.impl

import dev.azide.core.CausalLoopException
import dev.kmpx.collections.StableCollection
import dev.kmpx.collections.lists.LinkedList
import kotlin.jvm.JvmInline

abstract class AbstractLiveVertex : ListenableVertex {
    @JvmInline
    private value class LiveListenerHandle(
        val internalHandle: StableCollection.Handle<ListenableVertex.Listener>,
    ) : ListenableVertex.ListenerHandle

    private val _registeredListeners: LinkedList<ListenableVertex.Listener> = LinkedList()

    override val listenerCount: Int
        get() = _registeredListeners.size

    private var _isNotifyingListeners = false

    override fun registerListener(
        processingContext: Transactions.ProcessingContext,
        listener: ListenableVertex.Listener,
    ): ListenableVertex.ListenerHandle {
        val internalHandle = _registeredListeners.append(listener)

        if (_registeredListeners.size == 1) {
            onFirstListenerRegistered(
                processingContext = processingContext,
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

        _registeredListeners.removeVia(handleImpl.internalHandle)

        if (_registeredListeners.isEmpty()) {
            onLastListenerUnregistered()
        }
    }

    protected fun notifyListeners(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (_isNotifyingListeners) {
            throw CausalLoopException("Causal loop detected in vertex: $this")
        }

        try {
            _isNotifyingListeners = true

            _registeredListeners.forEach { listener ->
                val listenerStatus = listener.handle(
                    propagationContext = propagationContext,
                )

                // Remove the listener if it's unreachable
                listenerStatus == ListenableVertex.ListenerStatus.Unreachable
            }
        } finally {
            _isNotifyingListeners = false
        }
    }

    protected open fun onFirstListenerRegistered(
        processingContext: Transactions.ProcessingContext,
    ) {
    }

    protected open fun onLastListenerUnregistered() {
    }
}
