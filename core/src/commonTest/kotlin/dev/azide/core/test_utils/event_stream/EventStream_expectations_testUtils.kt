package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.test_utils.generic.AbstractExplicitExpectedTestSubjectReaction
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.ExpectedTestSubjectState
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition

typealias ExpectedEventStreamEmission<EventT> = ExpectedTestSubjectReaction<EventStream<EventT>, EventStreamVertex.Emission<EventT>>

typealias ExpectedBasicEventStreamEmission<EventT> = AbstractExplicitExpectedTestSubjectReaction<EventStream<EventT>, EventStreamVertex.Emission<EventT>>

typealias ExpectedEventStreamTransition<EventT> = ExpectedTestSubjectTransition<EventStream<EventT>, EventStreamVertex.Emission<EventT>>

fun <EventT> ExpectedEventStreamEmission<EventT>.asTransition() = object : ExpectedEventStreamTransition<EventT> {
    override val expectedOldState: ExpectedTestSubjectState<EventStream<EventT>> = ExpectedTestSubjectState.None

    override val expectedReaction: ExpectedEventStreamEmission<EventT> = this@asTransition

    override val expectedNewState: ExpectedTestSubjectState<EventStream<EventT>> = ExpectedTestSubjectState.None
}

abstract class AbstractExpectedEventStreamReaction<EventT> : ExpectedBasicEventStreamEmission<EventT>() {
    final override val expectedSubjectNotification: EventStreamVertex.Emission<EventT>?
        get() = expectedEffectiveEmission

    abstract val expectedEffectiveEmission: EventStreamVertex.Emission<EventT>?
}

@Suppress("ClassName")
object EventStream_expectations_testUtils {
    fun <EventT> expectEmission(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedEmittedEvent: EventT,
    ): ExpectedBasicEventStreamEmission<EventT> = object : AbstractExpectedEventStreamReaction<EventT>() {
        override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
            intermediatePropagationTolerance

        override val expectedEffectiveEmission: EventStreamVertex.Emission<EventT> = EventStreamVertex.Emission(
            emittedEvent = expectedEmittedEvent,
        )
    }

    fun <EventT> expectNoEmission(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedEventStreamEmission<EventT> = object : AbstractExpectedEventStreamReaction<EventT>() {
        override val expectedEffectiveEmission: EventStreamVertex.Emission<EventT>? = null

        override val intermediatePropagationTolerance = intermediatePropagationTolerance
    }
}
