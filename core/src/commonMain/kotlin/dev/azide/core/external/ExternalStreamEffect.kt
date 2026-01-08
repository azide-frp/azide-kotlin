package dev.azide.core.external

/**
 * Adapts an external resource which conceptually resembles an [dev.azide.core.Effect] with an [dev.azide.core.EventStream] result, i.e. is an
 * operation that can be started, **causing** events to occur over time.
 *
 * @param EventT the type of events emitted by the external effect.
 */
interface ExternalStreamEffect<EventT> {
    /**
     * Start the external effect bound to the given [handler], which should be called once per event emitted by the
     * external system as a part of the started effect. Once this effect is cancelled, the external effect should cease
     * and the handler should no longer be invoked.
     *
     * @return a handle to cancel the external effect.
     */
    fun start(
        handler: ExternalEventHandler<EventT>,
    ): ExternalEffectDelegate
}

fun <EventT> ExternalStreamEffect<EventT>.bind(
    handler: ExternalEventHandler<EventT>,
): ExternalSchedule = object : ExternalSchedule {
    override fun start(): ExternalEffectDelegate = this@bind.start(handler = handler)
}
