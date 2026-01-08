package dev.azide.core.external

/**
 * Handler for events occurring outside the reactive system.
 */
interface ExternalEventHandler<EventT> {
    /**
     * Handles the external event, typically starting a transaction and dispatching the event into the reactive system.
     */
    fun handle(event: EventT)
}
