package dev.azide.core

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.PureCellVertex
import dev.azide.core.impl.cell.effects.CellTriggerEverySchedule
import dev.azide.core.impl.cell.operated_vertices.ActuatedCellVertex
import dev.azide.core.impl.cell.operated_vertices.ExecutedEveryCellVertex
import dev.azide.core.impl.cell.operated_vertices.Mapped2CellVertex
import dev.azide.core.impl.cell.operated_vertices.MappedCellVertex
import dev.azide.core.impl.cell.operated_vertices.SwitchedCellVertex
import dev.azide.core.impl.effects.ExternalizedEffect
import dev.azide.core.impl.event_stream.operated_vertices.DivertedEventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.UpdatedValuesEventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.ValuesEventStreamVertex
import dev.azide.core.impl.utils.LoopClosure
import kotlin.jvm.JvmName

interface Cell<out ValueT> {
    val vertex: CellVertex<ValueT>

    class Const<out ValueT>(
        constValue: ValueT,
    ) : Cell<ValueT> {
        private val pureVertex = PureCellVertex(
            value = constValue,
        )

        override val vertex: CellVertex<ValueT>
            get() = pureVertex
    }

    class Ordinary<out ValueT> internal constructor(
        override val vertex: CellVertex<ValueT>,
    ) : Cell<ValueT>

    companion object {
        fun <ValueT1, ValueT2, ResultT> map2(
            cell1: Cell<ValueT1>,
            cell2: Cell<ValueT2>,
            transform: (ValueT1, ValueT2) -> ResultT,
        ): Cell<ResultT> = Ordinary(
            vertex = Mapped2CellVertex(
                sourceVertex1 = cell1.vertex,
                sourceVertex2 = cell2.vertex,
                transform = transform,
            ),
        )

        fun <ValueT1, ValueT2, ValueT3, ResultT> map3(
            cell1: Cell<ValueT1>,
            cell2: Cell<ValueT2>,
            cell3: Cell<ValueT3>,
            transform: (ValueT1, ValueT2, ValueT3) -> ResultT,
        ): Cell<ResultT> = TODO()

        fun <ValueT1, ValueT2, ValueT3, ValueT4, ResultT> map4(
            cell1: Cell<ValueT1>,
            cell2: Cell<ValueT2>,
            cell3: Cell<ValueT3>,
            cell4: Cell<ValueT4>,
            transform: (ValueT1, ValueT2, ValueT3, ValueT4) -> ResultT,
        ): Cell<ResultT> = TODO()

        fun <ValueT> defining(
            initialValue: ValueT,
            newValues: EventStream<ValueT>,
        ): Moment<Cell<ValueT>> = newValues.holding(
            initialValue = initialValue,
        )

        context(momentContext: MomentContext) fun <ValueT> define(
            initialValue: ValueT,
            newValues: EventStream<ValueT>,
        ): Cell<ValueT> = newValues.hold(
            initialValue = initialValue,
        )

        fun <ValueT> switch(
            outerCell: Cell<Cell<ValueT>>,
        ): Cell<ValueT> = Ordinary(
            SwitchedCellVertex(
                outerSourceVertex = outerCell.vertex,
            ),
        )

        fun <ValueT> divert(
            outerCell: Cell<EventStream<ValueT>>,
        ): EventStream<ValueT> = EventStream.Ordinary(
            vertex = DivertedEventStreamVertex(
                outerSourceVertex = outerCell.vertex,
            ),
        )
    }
}

val <ValueT> Cell<ValueT>.sampling: Moment<ValueT>
    get() = object : Moment<ValueT> {
        override fun pullInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): ValueT = vertex.getOldValue(
            processingContext = propagationContext,
        )
    }

val <ValueT> Cell<ValueT>.values: Moment<EventStream<ValueT>>
    get() = object : Moment<EventStream<ValueT>> {
        override fun pullInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): EventStream<ValueT> {
            val sourceVertex = vertex as? CellVertex ?: return EventStream.Never

            val valuesEventStreamVertex = ValuesEventStreamVertex.start(
                propagationContext = propagationContext,
                sourceVertex = sourceVertex,
            )

            return EventStream.Ordinary(
                vertex = valuesEventStreamVertex,
            )
        }
    }

val <ValueT> Cell<ValueT>.updatedValues: EventStream<ValueT>
    get() = EventStream.Ordinary(
        vertex = UpdatedValuesEventStreamVertex(
            sourceVertex = this.vertex,
        ),
    )

context(momentContext: MomentContext) fun <ValueT> Cell<ValueT>.sample(): ValueT {
    val propagationContext = momentContext.propagationContext

    return vertex.getOldValue(
        processingContext = propagationContext,
    )
}

fun <ValueT, TransformedValueT> Cell<ValueT>.map(
    transform: (ValueT) -> TransformedValueT,
): Cell<TransformedValueT> = Cell.Ordinary(
    vertex = MappedCellVertex(
        sourceVertex = this@map.vertex,
        transform = transform,
    ),
)

context(momentContext: MomentContext) fun <ValueT, TransformedValueT> Cell<ValueT>.mapAt(
    transform: context(MomentContext) (ValueT) -> TransformedValueT,
): Cell<TransformedValueT> = sampleEveryOf { value: ValueT ->
    Moment.decontextualize {
        transform(value)
    }
}.pullInContext()

fun <ValueT, TransformedValueT> Cell<ValueT>.divertOf(
    transform: (ValueT) -> EventStream<TransformedValueT>,
): EventStream<TransformedValueT> = Cell.divert(
    outerCell = map(transform),
)

fun <ValueT> Cell<Moment<ValueT>>.sampleEvery(): Moment<Cell<ValueT>> = sampling.joinOf { initialMoment: Moment<ValueT> ->
    initialMoment.joinOf { initialValue: ValueT ->
        updatedValues.sampleEach().holding(initialValue = initialValue)
    }
}

fun <ValueT, TransformedValueT> Cell<ValueT>.sampleEveryOf(
    transform: (ValueT) -> Moment<TransformedValueT>,
): Moment<Cell<TransformedValueT>> = map(transform).sampleEvery()

fun Cell<Trigger>.triggerEvery(): Schedule = ExternalizedEffect(
    internalEffect = CellTriggerEverySchedule(
        sourceActionCell = this@triggerEvery,
    ),
)

fun <ValueT> Cell<ValueT>.triggerEveryOf(
    transform: (ValueT) -> Trigger,
): Schedule = map(transform).triggerEvery()

fun <ResultT> Cell<Action<ResultT>>.executeEvery(): Effect<Cell<ResultT>> = ExternalizedEffect<Cell<ResultT>>(
    internalEffect = ExecutedEveryCellVertex.ExecutionEffect(
        sourceActionCell = this@executeEvery,
    ),
)

fun <ValueT, TransformedValueT> Cell<ValueT>.executeEveryOf(
    transform: (ValueT) -> Action<TransformedValueT>,
): Effect<Cell<TransformedValueT>> = map(transform).executeEvery()

fun <ValueT> Cell<ValueT>.sampleExternally(): ValueT = Transactions.executeWithResult { propagationContext ->
    vertex.getOldValue(
        processingContext = propagationContext,
    )
}

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

fun <ValueT> Cell<ValueT>.actuateOf(
    transform: (ValueT) -> Schedule,
): Schedule = map(transform).actuate()

@JvmName("actuateEffect")
fun <InnerResultT> Cell<Effect<InnerResultT>>.actuate(): Effect<Cell<InnerResultT>> =
    ExternalizedEffect<Cell<InnerResultT>>(
        internalEffect = ActuatedCellVertex.ActuationEffect(
            sourceEffectCell = this@actuate,
        ),
    )

@JvmName("actuateOfEffect")
fun <ValueT, TransformedValueT> Cell<ValueT>.actuateOf(
    transform: (ValueT) -> Effect<TransformedValueT>,
): Effect<Cell<TransformedValueT>> = map(transform).actuate()
