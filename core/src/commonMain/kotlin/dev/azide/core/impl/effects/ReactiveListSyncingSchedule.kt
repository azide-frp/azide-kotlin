package dev.azide.core.impl.effects

import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.applyTo
import dev.azide.core.impl.registerBoundListener

class ReactiveListSyncingSchedule<ElementT>(
    private val sourceReactiveList: ReactiveList<ElementT>,
    private val listSyncer: ListSyncer<ElementT>,
) : InternalSchedule {
    interface ListSyncer<ElementT> {
        class MutableListSyncer<ElementT>(
            private val mutableList: MutableList<ElementT>,
        ) : ListSyncer<ElementT> {
            override fun syncContent(
                content: List<ElementT>,
            ) {
                mutableList.clear()
                mutableList.addAll(content)
            }

            override fun syncChange(
                change: ListChange<ElementT>,
            ) {
                change.applyTo(mutableList)
            }
        }

        fun syncContent(
            content: List<ElementT>,
        )

        fun syncChange(
            change: ListChange<ElementT>,
        )
    }

    private enum class InternalState {
        PreSync,
        Attached,
        Detached,
        Disposed,
    }

    override fun startInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): InternalEffect.RevocableOutcome<Unit> = object : InternalEffect.RevocableOutcome<Unit> {
        private var internalState = InternalState.PreSync

        private var listenerHandle: Vertex.ListenerHandle? = null

        private var executionRevocable: Revocable? = null

        override val result = Unit

        override fun cancelInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Revocable {
            if (internalState == InternalState.PreSync) {
                val foundExecutionRevocable =
                    this.executionRevocable ?: throw IllegalStateException("Inconsistent internal state")

                foundExecutionRevocable.revoke()

                this.executionRevocable = null

                return object : Revocable {
                    override fun revoke() {
                        if (internalState == InternalState.Disposed) {
                            return
                        }

                        executionRevocable = enqueueInitialSyncExecution(
                            propagationContext = propagationContext,
                        )
                    }
                }
            }

            if (internalState != InternalState.Attached) {
                throw IllegalStateException("The syncing effect is not attached and thus cannot be cancelled.")
            }

            detach()

            executionRevocable?.revoke()
            executionRevocable = null

            return object : Revocable {
                /**
                 * Revoke the syncing effect cancellation.
                 */
                override fun revoke() {
                    if (internalState == InternalState.Disposed) {
                        return
                    }

                    attach(
                        propagationContext = propagationContext,
                        mode = ActivationMode.Online,
                    )
                }
            }
        }

        /**
         * Revoke the syncing effect start.
         */
        override fun revoke() {
            executionRevocable?.revoke()
            executionRevocable = null

            if (internalState == InternalState.Attached) {
                detach()
            }

            internalState = InternalState.Disposed
        }

        private fun attach(
            propagationContext: Transactions.PropagationContext,
            mode: ActivationMode,
        ) {
            if (listenerHandle != null) {
                throw IllegalStateException("The syncing effect is already attached.")
            }

            listenerHandle = sourceReactiveList.trackedVertex.registerBoundListener(
                propagationContext = propagationContext,
                listener = object : Vertex.BoundListener {
                    override fun handle(propagationContext: Transactions.PropagationContext) {
                        executionRevocable?.revoke()

                        val sourceOngoingChange = sourceReactiveList.trackedVertex.ongoingChange

                        executionRevocable = when (sourceOngoingChange) {
                            null -> null

                            else -> enqueueChangeApplicationForExecution(
                                propagationContext = propagationContext,
                                sourceOngoingChange = sourceOngoingChange,
                            )
                        }
                    }
                },
                mode = mode,
            )

            val sourceOngoingChange = sourceReactiveList.trackedVertex.ongoingChange

            if (sourceOngoingChange != null) {
                executionRevocable = enqueueChangeApplicationForExecution(
                    propagationContext = propagationContext,
                    sourceOngoingChange = sourceOngoingChange,
                )
            }

            internalState = InternalState.Attached
        }

        private fun detach() {
            val listenerHandle =
                this.listenerHandle ?: throw IllegalStateException("The syncing effect is not attached.")

            this.listenerHandle = null

            sourceReactiveList.trackedVertex.unregisterListener(
                handle = listenerHandle,
            )

            internalState = InternalState.Detached
        }

        private fun enqueueInitialSyncExecution(
            propagationContext: Transactions.PropagationContext,
        ): Revocable = propagationContext.enqueueCallbackForPostProcessing {
            val sourceInitialNewContent = sourceReactiveList.trackedVertex.getOldContentView(
                propagationContext = propagationContext,
            ).toMutableList()

            val sourceInitialChange = sourceReactiveList.trackedVertex.ongoingChange

            sourceInitialChange?.applyTo(mutableList = sourceInitialNewContent)

            propagationContext.enqueueForExecution {
                listSyncer.syncContent(content = sourceInitialNewContent)

                executionRevocable = null

                attach(
                    propagationContext = propagationContext, // HACK (we're past the propagation phase)
                    mode = ActivationMode.Offline,
                )
            }
        }

        private fun enqueueChangeApplicationForExecution(
            propagationContext: Transactions.PropagationContext,
            sourceOngoingChange: ListChange<ElementT>,
        ): Revocable = propagationContext.enqueueForExecution {
            listSyncer.syncChange(change = sourceOngoingChange)

            executionRevocable = null
        }

        init {
            wrapUpContext.enqueueForWrapUp { propagationContext ->
                executionRevocable = enqueueInitialSyncExecution(
                    propagationContext = propagationContext,
                )
            }
        }
    }
}
