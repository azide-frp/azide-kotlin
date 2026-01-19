package dev.azide.core.external

import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils

/**
 * Handler for events occurring outside the reactive system.
 */
interface ExternalEventHandler<EventT> {
    class Lazy<EventT>(
        private val handlerLazy: kotlin.Lazy<ExternalEventHandler<EventT>>,
    ) : ExternalEventHandler<EventT> {
        override fun handle(event: EventT) {
            handlerLazy.value.handle(event = event)
        }
    }

    companion object {
        fun <ResultT, LoopedEventT> looped(
            block: (ExternalEventHandler<LoopedEventT>) -> LoopClosure<ResultT, ExternalEventHandler<LoopedEventT>>,
        ): ResultT = LoopUtils.looped { loopedEventHandlerLazy: kotlin.Lazy<ExternalEventHandler<LoopedEventT>> ->
            block(
                Lazy(
                    handlerLazy = loopedEventHandlerLazy,
                ),
            )
        }
    }

    /**
     * Handles the external event, typically starting a transaction and dispatching the event into the reactive system.
     */
    fun handle(event: EventT)
}
