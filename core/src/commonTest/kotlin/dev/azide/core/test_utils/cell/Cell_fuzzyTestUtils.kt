package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.TestSequentialStimulation
import kotlin.random.Random

@Suppress("ClassName")
object Cell_fuzzyTestUtils {
    fun <ValueT> buildAppropriateInputCellStimulationSequence(
        random: Random,
        intermediateValueGenerator: RandomValueGenerator<ValueT>,
        inputCell: TestInputCell<ValueT>,
        oldValue: ValueT,
        newValue: ValueT,
    ): TestSequentialStimulation? {
        fun buildSingleExtraRandomRevokedSequence(): TestSequentialStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputCellStimulationSequence(
                    random = random,
                    intermediateValueGenerator = intermediateValueGenerator,
                    inputCell = inputCell,
                )

                else -> null
            }
        }

        return when {
            oldValue == newValue -> TestSequentialStimulation.concatAll(
                sequences = listOfNotNull(
                    // Build up to two ineffective revoked change sequences
                    buildSingleExtraRandomRevokedSequence(),
                    buildSingleExtraRandomRevokedSequence(),
                ),
            )

            else -> buildRandomEffectiveInputCellStimulationSequence(
                random = random,
                intermediateValueGenerator = intermediateValueGenerator,
                inputCell = inputCell,
                newValue = newValue,
            )
        }
    }

    private fun <ValueT> buildRandomEffectiveInputCellStimulationSequence(
        random: Random,
        intermediateValueGenerator: RandomValueGenerator<ValueT>,
        inputCell: TestInputCell<ValueT>,
        newValue: ValueT,
    ): TestSequentialStimulation {
        fun buildSingleExtraRandomRevokedStimulationSequence(): TestSequentialStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputCellStimulationSequence(
                    random = random,
                    intermediateValueGenerator = intermediateValueGenerator,
                    inputCell = inputCell,
                )

                else -> null
            }
        }

        fun buildSingleExtraRandomCorrectionStimulation(): TestStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> inputCell.correctUpdate(
                    correctedNewValue = intermediateValueGenerator.next(),
                )

                else -> null
            }
        }

        fun buildFinalStimulationSequence(): TestSequentialStimulation {
            val r = random.nextDouble()

            return when {
                // Final change is correction
                r < 0.3 -> TestSequentialStimulation(
                    consecutiveStimulations = listOfNotNull(
                        inputCell.update(
                            newValue = intermediateValueGenerator.next(),
                        ),
                        // Build a potential extra correction update
                        buildSingleExtraRandomCorrectionStimulation(),
                        inputCell.correctUpdate(
                            correctedNewValue = newValue,
                        ),
                    ),
                )

                // Final change is the initial change
                else -> TestSequentialStimulation(
                    consecutiveStimulations = listOf(
                        inputCell.update(
                            newValue = newValue,
                        ),
                    ),
                )
            }
        }

        return TestSequentialStimulation.concatAll(
            sequences = listOfNotNull(
                // Build up to two extra random revoked change sequences which should be totally ineffective
                buildSingleExtraRandomRevokedStimulationSequence(),
                buildSingleExtraRandomRevokedStimulationSequence(),
                // Build the final stimulation sequence
                buildFinalStimulationSequence()
            ),
        )!!
    }

    private fun <ValueT> buildRandomRevokedInputCellStimulationSequence(
        random: Random,
        intermediateValueGenerator: RandomValueGenerator<ValueT>,
        inputCell: TestInputCell<ValueT>,
    ): TestSequentialStimulation {
        fun buildSingleExtraRandomCorrectionUpdateStimulation(): TestStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> inputCell.correctUpdate(
                    correctedNewValue = intermediateValueGenerator.next(),
                )

                else -> null
            }
        }

        return TestSequentialStimulation(
            consecutiveStimulations = listOfNotNull(
                // We build at least one intermediate update
                inputCell.update(
                    newValue = intermediateValueGenerator.next(),
                ),
                // But, with a small chance, we build up to two extra intermediate (correction) updates
                buildSingleExtraRandomCorrectionUpdateStimulation(),
                buildSingleExtraRandomCorrectionUpdateStimulation(),
                // Finally, the update sequence is revoked
                inputCell.revokeUpdate(),
            ),
        )
    }
}
