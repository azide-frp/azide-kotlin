package dev.azide.core.external

/**
 * An external entity which conceptually resembles a [dev.azide.core.Schedule], i.e. an effect that doesn't have a specific result
 * associated.
 */
interface ExternalSchedule {
    /**
     * Start the external schedule, i.e. cause some external effect to begin occurring. Once this schedule is cancelled,
     * the external effect should cease.
     *
     * @return a handle to cancel the external schedule.
     */
    fun start(): ExternalEffectDelegate
}
