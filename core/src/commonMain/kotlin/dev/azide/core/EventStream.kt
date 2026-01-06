package dev.azide.core

import dev.azide.core.internal.RevocationHandle
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.operated_vertices.HeldCellVertex
import dev.azide.core.internal.effects.AbstractExecutionMergingTrigger
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.TerminatedEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.ExecutedEachEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.FilteredEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.MappedEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.SingleEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.WrappedExternalEventStreamVertex
import dev.azide.core.internal.utils.LoopClosure
import dev.azide.core.internal.utils.LoopUtils
import kotlin.jvm.JvmName

interface EventStream<out EventT> {
    val vertex: EventStreamVertex<EventT>

    object Never : EventStream<Nothing> {
        override val vertex: EventStreamVertex<Nothing> = TerminatedEventStreamVertex()
    }

    class Ordinary<EventT> internal constructor(
        override val vertex: EventStreamVertex<EventT>,
    ) : EventStream<EventT>

    class Lazy<EventT> internal constructor(
        private val eventStreamLazy: kotlin.Lazy<EventStream<EventT>>,
    ) : EventStream<EventT> {
        override val vertex: EventStreamVertex<EventT> by lazy {
            eventStreamLazy.value.vertex
        }
    }

    companion object {
        fun <EventT> wrap(
            externalSourceAdapter: ExternalSourceAdapter<EventT>,
        ): EventStream<EventT> = Ordinary(
            vertex = WrappedExternalEventStreamVertex(
                externalSourceAdapter = externalSourceAdapter,
            ),
        )

        fun <ResultT, LoopedEventT> looped(
            block: (EventStream<LoopedEventT>) -> LoopClosure<ResultT, EventStream<LoopedEventT>>,
        ): ResultT = LoopUtils.looped { loopedEventStreamLazy ->
            block(
                Lazy(
                    eventStreamLazy = loopedEventStreamLazy,
                ),
            )
        }

        fun <ResultT, LoopedEventT> loopedInMoment(
            block: (EventStream<LoopedEventT>) -> Moment<LoopClosure<ResultT, EventStream<LoopedEventT>>>,
        ): Moment<ResultT> = Moment.looped { loopedEventStreamLazy ->
            block(
                Lazy(
                    eventStreamLazy = loopedEventStreamLazy,
                ),
            )
        }

        fun <ResultT, LoopedEventT> loopedInAction(
            block: (EventStream<LoopedEventT>) -> Action<LoopClosure<ResultT, EventStream<LoopedEventT>>>,
        ): Action<ResultT> = Action.looped { loopedEventStreamLazy ->
            block(
                Lazy(
                    eventStreamLazy = loopedEventStreamLazy,
                ),
            )
        }

        fun <EventT> merge2(
            eventStream1: EventStream<EventT>,
            eventStream2: EventStream<EventT>,
        ): EventStream<EventT> = TODO()

        fun <EventT> merge3(
            eventStream1: EventStream<EventT>,
            eventStream2: EventStream<EventT>,
            eventStream3: EventStream<EventT>,
        ): EventStream<EventT> = TODO()
    }
}

fun <EventT, TransformedEventT> EventStream<EventT>.map(
    transform: (EventT) -> TransformedEventT,
): EventStream<TransformedEventT> = EventStream.Ordinary(
    vertex = when (val sourceVertex = this.vertex) {
        is LiveEventStreamVertex -> MappedEventStreamVertex(
            sourceVertex = sourceVertex,
            transform = { _, event ->
                transform(event)
            },
        )

        is TerminatedEventStreamVertex -> TerminatedEventStreamVertex()
    },
)

fun <EventT, TransformedEventT : Any> EventStream<EventT>.mapNotNull(
    transform: (EventT) -> TransformedEventT?,
): EventStream<TransformedEventT> = TODO()

fun <EventT, TransformedEventT> EventStream<EventT>.mapAt(
    transform: context(MomentContext) (EventT) -> TransformedEventT,
): EventStream<TransformedEventT> = EventStream.Ordinary(
    vertex = when (val sourceVertex = this.vertex) {
        is LiveEventStreamVertex -> MappedEventStreamVertex(
            sourceVertex = sourceVertex,
            transform = { propagationContext, event ->
                MomentContext.wrapUp(
                    propagationContext,
                ) {
                    transform(event)
                }
            },
        )

        is TerminatedEventStreamVertex -> TerminatedEventStreamVertex()
    },
)

fun <EventT, TransformedEventT : Any> EventStream<EventT>.mapNotNullAt(
    transform: context(MomentContext) (EventT) -> TransformedEventT?,
): EventStream<TransformedEventT> = TODO()

fun <EventT> EventStream<EventT>.filter(
    predicate: (EventT) -> Boolean,
): EventStream<EventT> = EventStream.Ordinary(
    vertex = when (val sourceVertex = this.vertex) {
        is LiveEventStreamVertex -> FilteredEventStreamVertex(
            sourceVertex = sourceVertex,
            predicate = predicate,
        )

        is TerminatedEventStreamVertex -> TerminatedEventStreamVertex()
    },
)

fun <EventT> EventStream<EventT>.filterAt(
    predicate: context(MomentContext) (EventT) -> Boolean,
): EventStream<EventT> = TODO()

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.single(): EventStream<EventT> =
    EventStream.Ordinary(
        vertex = when (val sourceVertex = this.vertex) {
            is LiveEventStreamVertex -> SingleEventStreamVertex(
                propagationContext = momentContext.propagationContext,
                sourceVertex = sourceVertex,
            )

            is TerminatedEventStreamVertex -> TerminatedEventStreamVertex()
        }
    )

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.take(
    count: Int,
): EventStream<EventT> = TODO()

fun <EventT> EventStream<EventT>.holding(
    initialValue: EventT,
): Moment<Cell<EventT>> = object : Moment<Cell<EventT>> {
    override fun pullInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Cell<EventT> = Cell.Ordinary(
        vertex = HeldCellVertex.start(
            wrapUpContext = wrapUpContext,
            sourceEventStream = this@holding,
            initialValue = initialValue,
        ),
    )
}

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.hold(
    initialValue: EventT,
): Cell<EventT> = holding(
    initialValue = initialValue,
).pullInternally(
    propagationContext = momentContext.propagationContext,
    wrapUpContext = momentContext.wrapUpContext,
)

context(momentContext: MomentContext) fun <EventT, AccT> EventStream<EventT>.accumulate(
    initialAccValue: AccT,
    transform: (accValue: AccT, newEvent: EventT) -> AccT,
): Cell<AccT> = EventStream.looped<Cell<AccT>, AccT> { loopedNewAccValues ->
    val accCell = Cell.define(
        initialValue = initialAccValue,
        newValues = loopedNewAccValues,
    )

    val newAccValues = this@accumulate.mapAt { newEvent ->
        transform(
            accCell.sample(),
            newEvent,
        )
    }

    LoopClosure(
        result = accCell,
        loopedValue = newAccValues,
    )
}

fun <EventT> EventStream<Action<EventT>>.executeEach(): Effect<EventStream<EventT>> =
    object : Effect<EventStream<EventT>> {
        override val start: Action<Effect.Outcome<EventStream<EventT>>> =
            object : Action<Effect.Outcome<EventStream<EventT>>> {
                override fun executeInternally(
                    propagationContext: Transactions.PropagationContext,
                    wrapUpContext: Transactions.WrapUpContext,
                ): Action.Outcome<Effect.Outcome<EventStream<EventT>>> {
                    val sourceVertex = this@executeEach.vertex as? LiveEventStreamVertex ?: return Action.Outcome.of(
                        Effect.Outcome.of(
                            result = EventStream.Never,
                            handle = Effect.Handle.Noop,
                        ),
                        RevocationHandle.Noop,
                    )

                    val executedEachEventStreamVertex = ExecutedEachEventStreamVertex.start(
                        propagationContext = propagationContext,
                        sourceVertex = sourceVertex,
                    )

                    val resultEventStream: EventStream<EventT> = EventStream.Ordinary(
                        vertex = executedEachEventStreamVertex,
                    )

                    val resultEffectHandle: Effect.Handle = object : Effect.Handle {
                        override val cancel: Trigger = object : AbstractExecutionMergingTrigger() {
                            override fun executeInternallyOnce(
                                propagationContext: Transactions.PropagationContext,
                                wrapUpContext: Transactions.WrapUpContext,
                            ): RevocationHandle {
                                if (executedEachEventStreamVertex.isShutdown) {
                                    return RevocationHandle.Noop
                                }

                                executedEachEventStreamVertex.stop()

                                return object : RevocationHandle {
                                    override fun revoke() {
                                        if (executedEachEventStreamVertex.isShutdown) {
                                            // The cancel action was revoked after the start action was revoked
                                            return
                                        }

                                        executedEachEventStreamVertex.restart(
                                            propagationContext = propagationContext,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    return Action.Outcome.of(
                        result = Effect.Outcome.of(
                            result = resultEventStream,
                            handle = resultEffectHandle,
                        ),
                        revocationHandle = object : RevocationHandle {
                            override fun revoke() {
                                executedEachEventStreamVertex.shutDown()
                            }
                        },
                    )
                }
            }
    }

fun EventStream<Trigger>.triggerEach(): Schedule = executeEach().map { }

fun <EventT> EventStream<Action<EventT>>.executeEachForever(): Action<EventStream<EventT>> =
    executeEach().start.map { outcome -> outcome.result }

fun EventStream<Trigger>.triggerEachForever(): Trigger = executeEachForever().map { }

fun <EventT, TransformedEventT> EventStream<EventT>.executeEachOf(
    transform: (EventT) -> Action<TransformedEventT>,
): Effect<EventStream<TransformedEventT>> = map(transform).executeEach()

@JvmName("actuateSchedule")
fun Cell<Schedule>.actuate(): Schedule = object : AbstractSchedule() {
    override val launchImpl: Action<Effect.Handle> = run {
        // Define the launching action of the schedule

        val newSchedules: EventStream<Schedule> = this@actuate.updatedValues

        this@actuate.sampling.joinOf { initialInnerSchedule: Schedule ->
            // Launch the initial schedule
            initialInnerSchedule.launch.joinOf { initialInnerScheduleHandle: Effect.Handle ->
                EventStream.loopedInAction { loopedNewInnerScheduleHandles: EventStream<Effect.Handle> ->
                    // Hold the handles to the new started schedules, as we need the handle to the currently active
                    // schedule to cancel it later
                    loopedNewInnerScheduleHandles.holding(
                        initialValue = initialInnerScheduleHandle,
                    ).joinOf { currentScheduleHandle: Cell<Effect.Handle> ->
                        // Define the transition effect that cancels the old schedule and starts the new one whenever a
                        // new schedule arrives
                        val transitionEffect: Effect<EventStream<Effect.Handle>> =
                            newSchedules.executeEachOf { updatedSchedule: Schedule ->
                                currentScheduleHandle.sampling.joinOf { currentScheduleHandleNow: Effect.Handle ->
                                    // Cancel the old schedule...
                                    currentScheduleHandleNow.cancel.joinOf {
                                        // ...and immediately start the new one
                                        updatedSchedule.launch
                                    }

                                    // Note that in the corner case, if the source schedule cell updates at the moment
                                    // the outer schedule launches, these three actions happen simultaneously: the
                                    // initial schedule starts, is immediately cancelled, and the updated schedule
                                    // starts.
                                }
                            }

                        // Start the transition effect
                        transitionEffect.start.joinOf { transitionEffectOutcome ->
                            val newInnerScheduleHandles = transitionEffectOutcome.result
                            val transitionEffectHandle = transitionEffectOutcome.handle

                            val cancelCurrentScheduleTrigger: Trigger =
                                currentScheduleHandle.sampling.joinOf { currentScheduleHandleNow: Effect.Handle ->
                                    currentScheduleHandleNow.cancel
                                }

                            // Build the handle to the outer schedule (the one we're defining)
                            Effect.Handle.of(
                                cancelOnce = Triggers.combine(
                                    // Canceling the transition effect...
                                    transitionEffectHandle.cancel,
                                    // ...and the currently active inner schedule
                                    cancelCurrentScheduleTrigger,
                                ),
                            ).map { outerEffectHandle ->
                                LoopClosure(
                                    result = outerEffectHandle,
                                    loopedValue = newInnerScheduleHandles, // Close the loop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@JvmName("actuateEffect")
fun <ResultT> Cell<Effect<ResultT>>.actuate(): Effect<Cell<ResultT>> = object : Effect<Cell<ResultT>> {
    override val start = run {
        // Define the starting action of the effect

        val newInnerEffects: EventStream<Effect<ResultT>> = this@actuate.updatedValues

        this@actuate.sampling.joinOf { initialInnerEffect: Effect<ResultT> ->
            // Start the initial effect
            initialInnerEffect.start.joinOf { initialEffectOutcome ->
                val initialResult: ResultT = initialEffectOutcome.result
                val initialEffectHandle: Effect.Handle = initialEffectOutcome.handle

                EventStream.loopedInAction { loopedNewInnerEffectHandles: EventStream<Effect.Handle> ->
                    // Hold the handles to the new started effects, as we need the handle to the currently active
                    // effect to cancel it later
                    loopedNewInnerEffectHandles.holding(
                        initialValue = initialEffectHandle,
                    ).joinOf { currentInnerEffectHandle: Cell<Effect.Handle> ->
                        // Define the transition effect that cancels the old effect and starts the new one whenever a
                        // new effect arrives
                        val transitionEffect: Effect<EventStream<Effect.Outcome<ResultT>>> =
                            newInnerEffects.executeEachOf { newInnerEffect: Effect<ResultT> ->
                                currentInnerEffectHandle.sampling.joinOf { currentInnerEffectHandleNow: Effect.Handle ->
                                    // Cancel the old effect...
                                    currentInnerEffectHandleNow.cancel.joinOf {
                                        // ...and immediately start the new one
                                        newInnerEffect.start
                                    }

                                    // Note that in the corner case, if the source effect cell updates at the moment
                                    // the outer effect starts, these three actions happen simultaneously: the initial
                                    // effect starts, is immediately cancelled, and the updated effect starts.
                                }
                            }

                        // Start the transition effect
                        transitionEffect.start.joinOf { transitionEffectOutcome ->
                            val newInnerEffectOutcomes: EventStream<Effect.Outcome<ResultT>> =
                                transitionEffectOutcome.result
                            val transitionEffectHandle = transitionEffectOutcome.handle

                            val newInnerEffectResults = newInnerEffectOutcomes.map { it.result }
                            val newInnerEffectHandles = newInnerEffectOutcomes.map { it.handle }

                            val cancelCurrentInnerEffectTrigger: Trigger =
                                currentInnerEffectHandle.sampling.joinOf { currentInnerEffectHandleNow: Effect.Handle ->
                                    currentInnerEffectHandleNow.cancel
                                }

                            // Build the handle to the outer effect (the one we're defining)
                            Effect.Handle.of(
                                cancelOnce = Triggers.combine(
                                    // Canceling the transition effect...
                                    transitionEffectHandle.cancel,
                                    // ...and the currently active inner effect
                                    cancelCurrentInnerEffectTrigger,
                                ),
                            ).joinOf { outerEffectHandle ->
                                newInnerEffectResults.holding(
                                    initialValue = initialResult,
                                ).map { outerEffectResult: Cell<ResultT> ->
                                    LoopClosure(
                                        // Return the outer effect's result with the corresponding handle
                                        result = Effect.Outcome.of(
                                            result = outerEffectResult,
                                            handle = outerEffectHandle,
                                        ),
                                        loopedValue = newInnerEffectHandles, // Close the loop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun <T1, T2> EventStream<Pair<T1, T2>>.split(): Pair<EventStream<T1>, EventStream<T2>> = Pair(
    first = this.map { it.first },
    second = this.map { it.second },
)
