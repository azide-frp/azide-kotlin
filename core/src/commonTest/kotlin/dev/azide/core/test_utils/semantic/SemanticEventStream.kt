package dev.azide.core.test_utils.semantic

import kotlin.random.Random

interface SemanticEventStream<out LabelT : SemanticEventStream.Label, out EventT> {
    interface Label {
        data object Dependent : Label
    }

    val label: LabelT

    /** Returns the emitted event at the given timestamp or null if there is no emission. */
    fun evaluate(timestamp: Timestamp): EventT?

    companion object {
        fun <EventT> generateRandom(
            label: Label,
            random: Random,
            randomValueGenerator: dev.azide.core.test_utils.RandomValueGenerator<EventT>,
        ): SemanticEventStream<Label, EventT> = object : SemanticEventStream<Label, EventT> {
            override val label: Label = label

            // Keep a cache of nullable emitted events per timestamp. Start with no emission at t=0.
            private val cache: MutableList<EventT?> = mutableListOf(null)

            override fun evaluate(timestamp: Timestamp): EventT? {
                while (cache.size <= timestamp.t) {
                    val next = if (random.nextDouble() < 0.5) {
                        randomValueGenerator.next()
                    } else {
                        // no emission at this timestamp: preserve last known state (which may be null)
                        cache.last()
                    }

                    cache += next
                }

                return cache[timestamp.t]
            }
        }

        fun <EventT, TransformedEventT> map(
            semanticEventStream: AnySemanticEventStream<EventT>,
            transform: (EventT) -> TransformedEventT,
        ): AnySemanticEventStream<TransformedEventT> = mapAt(
            semanticEventStream = semanticEventStream,
            transform = { event, _ -> transform(event) },
        )

        fun <EventT, TransformedEventT> mapAt(
            semanticEventStream: AnySemanticEventStream<EventT>,
            transform: (EventT, Timestamp) -> TransformedEventT,
        ): AnySemanticEventStream<TransformedEventT> = object : AnySemanticEventStream<TransformedEventT> {
            override val label: Label = Label.Dependent

            override fun evaluate(timestamp: Timestamp): TransformedEventT? {
                val event = semanticEventStream.evaluate(timestamp = timestamp)
                return event?.let { transform(it, timestamp) }
            }
        }
    }
}

typealias AnySemanticEventStream<EventT> = SemanticEventStream<SemanticEventStream.Label, EventT>
