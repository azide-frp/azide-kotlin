package dev.azide.core.external

/**
 * An external entity which conceptually resembles an [dev.azide.core.EventStream], i.e. is a source of events occurring in time. The
 * events emitted by the source should not depend on registration/unregistration of listeners; they should occur
 * naturally on their own. If this isn't the case, consider using [ExternalStreamEffect] instead.
 */
interface ExternalStream<EventT> {
    /**
     * Delegate to manage the external subscription.
     */
    interface SubscriptionDelegate {
        /**
         * Register the external subscription. Once the subscription is registered, the handler passed to
         * [ExternalStream.bind] should start receiving events. The registration itself shouldn't cause the external
         * system to change its behavior in an observable way, internally activating the entity as necessary.
         */
        fun register()

        /**
         * Unregister the external subscription. Once the subscription is unregistered, the handler passed to
         * [ExternalStream.bind] should stop receiving events. The unregistration itself shouldn't cause the external
         * system to change its behavior in an observable way, but the entity might internally enter into a suspended
         * state to optimize resource usage.
         */
        fun unregister()
    }

    /**
     * Bind the external event source with the given [handler]. Binding shouldn't cause the external system to change
     * its behavior in an observable way.
     *
     * @return a delegate to manage the subscription.
     */
    fun bind(
        handler: ExternalEventHandler<EventT>,
    ): SubscriptionDelegate
}
