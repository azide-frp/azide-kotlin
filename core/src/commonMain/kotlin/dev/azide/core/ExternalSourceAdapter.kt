package dev.azide.core

/**
 * External event source adapter.
 */
interface ExternalSourceAdapter<EventT> {
    /**
     * Distributor to distribute external events within the reactive system.
     */
    interface EventDistributor<EventT> {
        /**
         * Distribute the external event within the reactive system, starting a transaction.
         */
        fun distribute(event: EventT)
    }

    /**
     * Handle to manage the external subscription.
     */
    interface SubscriptionHandle {
        /**
         * Register the external subscription. The registration itself shouldn't cause the external system to change its
         * behavior in an observable way.
         */
        fun register()

        /**
         * Unregister the external subscription. The unregistration itself shouldn't cause the external system to change
         * its behavior in an observable way.
         */
        fun unregister()
    }

    /**
     * Bind the external event source with the given [eventDistributor].
     *
     * @return a handle to manage the subscription.
     */
    fun bind(
        eventDistributor: EventDistributor<EventT>,
    ): SubscriptionHandle
}
