package dev.azide.core.internal.cell

import dev.azide.core.internal.FinalizationTransactionRegistry
import dev.azide.core.internal.Transaction
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex.Observer
import dev.azide.core.internal.cell.CellVertex.ObserverHandle
import dev.azide.core.internal.cell.CellVertex.ObserverStatus
import dev.azide.core.internal.cell.CellVertex.Update
import dev.azide.core.internal.utils.weak_bag.MutableBag
import dev.kmpx.platform.PlatformWeakReference
import kotlin.jvm.JvmInline

interface WarmCellVertex<out ValueT> : CellVertex<ValueT> {
    interface BasicObserver<in ValueT> : Observer<ValueT> {
        override fun handleUpdateWithStatus(
            propagationContext: Transactions.PropagationContext,
            update: Update<ValueT>?,
        ): ObserverStatus {
            handleUpdate(
                propagationContext = propagationContext,
                update = update,
            )

            return ObserverStatus.Reachable
        }

        fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
            update: Update<ValueT>?,
        )
    }

    class WeaklyReferencedObserver<ValueT>(
        basicObserver: BasicObserver<ValueT>,
    ) : Observer<ValueT> {
        private val basicObserverWeakReference = PlatformWeakReference(basicObserver)

        override fun handleUpdateWithStatus(
            propagationContext: Transactions.PropagationContext,
            update: Update<ValueT>?,
        ): ObserverStatus {
            when (val basicObserver = basicObserverWeakReference.get()) {
                null -> {
                    return ObserverStatus.Unreachable
                }

                else -> {
                    basicObserver.handleUpdate(
                        propagationContext = propagationContext,
                        update = update,
                    )

                    return ObserverStatus.Reachable
                }
            }
        }
    }

    @JvmInline
    value class WarmObserverHandle<ValueT>(
        internal val internalHandle: MutableBag.Handle<Observer<ValueT>>,
    ) : ObserverHandle

    interface WeakObserverHandle {
        fun cancel()
    }
}

fun <ValueT> WarmCellVertex.BasicObserver<ValueT>.weaklyReferenced(): WarmCellVertex.WeaklyReferencedObserver<ValueT> =
    WarmCellVertex.WeaklyReferencedObserver(
        basicObserver = this,
    )

/**
 * Analogical to [dev.azide.core.internal.event_stream.registerSubscriberWeakly].
 */
fun <ValueT> WarmCellVertex<ValueT>.registerObserverWeakly(
    propagationContext: Transactions.PropagationContext,
    dependentVertex: CellVertex<*>,
    observer: WarmCellVertex.BasicObserver<ValueT>,
): WarmCellVertex.WeakObserverHandle {
    val innerObserverHandle: ObserverHandle = registerObserver(
        propagationContext = propagationContext,
        observer = observer.weaklyReferenced(),
    )

    val finalizationHandle: FinalizationTransactionRegistry.Handle = FinalizationTransactionRegistry.register(
        target = dependentVertex,
        finalizationTransaction = object : Transaction<Unit>() {
            override fun propagate(
                propagationContext: Transactions.PropagationContext,
            ) {
                unregisterObserver(
                    handle = innerObserverHandle,
                )
            }
        },
    )

    return object : WarmCellVertex.WeakObserverHandle {
        override fun cancel() {
            unregisterObserver(
                handle = innerObserverHandle,
            )

            finalizationHandle.unregister()
        }
    }
}
