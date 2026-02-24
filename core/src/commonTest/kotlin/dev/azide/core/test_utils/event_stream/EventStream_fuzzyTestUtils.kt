package dev.azide.core.test_utils.event_stream

import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSequentialStimulation
import dev.azide.core.test_utils.TestStimulation
import kotlin.random.Random

@Suppress("ClassName")
object EventStream_fuzzyTestUtils {
    fun <EventT> buildRandomInputEventStreamStimulationSequence(
        random: Random,
        noiseValueGenerator: RandomValueGenerator<EventT>,
        inputEventStream: TestInputEventStream<EventT>,
        semanticEmission: EventT?,
    ): TestSequentialStimulation? {
        fun buildSingleExtraRandomRevokedSequence(): TestSequentialStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputEventStreamStimulationSequence(
                    random = random,
                    noiseValueGenerator = noiseValueGenerator,
                    inputEventStream = inputEventStream,
                )

                else -> null
            }
        }

        return when (semanticEmission) {
            null -> TestSequentialStimulation.concatAll(
                sequences = listOfNotNull(
                    buildSingleExtraRandomRevokedSequence(),
                    buildSingleExtraRandomRevokedSequence(),
                ),
            )

            else -> buildRandomEffectiveInputEventStreamStimulationSequence(
                random = random,
                intermediateValueGenerator = noiseValueGenerator,
                inputEventStream = inputEventStream,
                newEvent = semanticEmission,
            )
        }
    }

    private fun <EventT> buildRandomEffectiveInputEventStreamStimulationSequence(
        random: Random,
        intermediateValueGenerator: RandomValueGenerator<EventT>,
        inputEventStream: TestInputEventStream<EventT>,
        newEvent: EventT,
    ): TestSequentialStimulation {
        fun buildSingleExtraRandomRevokedStimulationSequence(): TestSequentialStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputEventStreamStimulationSequence(
                    random = random,
                    noiseValueGenerator = intermediateValueGenerator,
                    inputEventStream = inputEventStream,
                )

                else -> null
            }
        }

        fun buildSingleExtraRandomCorrectionStimulation(): TestStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> inputEventStream.correctEmission(
                    correctedEmittedEvent = intermediateValueGenerator.next(),
                )

                else -> null
            }
        }

        fun buildFinalStimulationSequence(): TestSequentialStimulation {
            val r = random.nextDouble()

            return when {
                // Final emission is correction
                r < 0.3 -> TestSequentialStimulation(
                    consecutiveStimulations = listOfNotNull(
                        inputEventStream.emit(
                            emittedEvent = intermediateValueGenerator.next(),
                        ),
                        // potential extra correction
                        buildSingleExtraRandomCorrectionStimulation(),
                        inputEventStream.correctEmission(
                            correctedEmittedEvent = newEvent,
                        ),
                    ).filterNotNull(),
                )

                // Final emission is the initial emission
                else -> TestSequentialStimulation(
                    consecutiveStimulations = listOf(
                        inputEventStream.emit(
                            emittedEvent = newEvent,
                        ),
                    ),
                )
            }
        }

        return TestSequentialStimulation.concatAll(
            sequences = listOfNotNull(
                buildSingleExtraRandomRevokedStimulationSequence(),
                buildSingleExtraRandomRevokedStimulationSequence(),
                buildFinalStimulationSequence(),
            ),
        )!!
    }

    private fun <EventT> buildRandomRevokedInputEventStreamStimulationSequence(
        random: Random,
        noiseValueGenerator: RandomValueGenerator<EventT>,
        inputEventStream: TestInputEventStream<EventT>,
    ): TestSequentialStimulation {
        fun buildSingleExtraRandomCorrectionEmissionStimulation(): TestStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> inputEventStream.correctEmission(
                    correctedEmittedEvent = noiseValueGenerator.next(),
                )

                else -> null
            }
        }

        return TestSequentialStimulation(
            consecutiveStimulations = listOfNotNull(
                inputEventStream.emit(
                    emittedEvent = noiseValueGenerator.next(),
                ),
                buildSingleExtraRandomCorrectionEmissionStimulation(),
                buildSingleExtraRandomCorrectionEmissionStimulation(),
                inputEventStream.revokeEmission(),
            ),
        )
    }
}
