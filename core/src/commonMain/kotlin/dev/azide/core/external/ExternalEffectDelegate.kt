package dev.azide.core.external

/**
 * Delegate to manage an effect outside the reactive system.
 */
interface ExternalEffectDelegate {
    /**
     * Cancel the external effect. Any resources associated with the effect should be released. Cancelling the effect
     * more than once should have no additional effect.
     */
    fun cancel()
}
