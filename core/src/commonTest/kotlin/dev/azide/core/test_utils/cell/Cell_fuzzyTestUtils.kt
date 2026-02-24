package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSequentialStimulation
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.semantic.SemanticCell
import kotlin.random.Random

@Suppress("ClassName")
object Cell_fuzzyTestUtils {
    fun <InputValueT> buildRandomInputCellStimulationSequence(
        random: Random,
        noiseValueGenerator: RandomValueGenerator<InputValueT>,
        inputCell: TestInputCell<InputValueT>,
        oldValue: InputValueT,
        newValue: InputValueT,
    ): TestSequentialStimulation? = buildRandomInputCellStimulationSequence(
        random = random,
        noiseValueGenerator = noiseValueGenerator,
        inputCell = inputCell,
        semanticInputTransition = SemanticCell.Transition.Update(
            oldValue = oldValue,
            updatedValue = newValue,
        ),
    )

    fun <InputValueT> buildRandomInputCellStimulationSequence(
        random: Random,
        noiseValueGenerator: RandomValueGenerator<InputValueT>,
        inputCell: TestInputCell<InputValueT>,
        semanticInputTransition: SemanticCell.Transition<InputValueT>,
    ): TestSequentialStimulation? {
        fun buildSingleExtraRandomRevokedSequence(): TestSequentialStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputCellStimulationSequence(
                    random = random,
                    noiseValueGenerator = noiseValueGenerator,
                    inputCell = inputCell,
                )

                else -> null
            }
        }

        return when (semanticInputTransition) {
            is SemanticCell.Transition.Pass -> {
                TestSequentialStimulation.concatAll(
                    sequences = listOfNotNull(
                        // Build up to two ineffective revoked change sequences
                        buildSingleExtraRandomRevokedSequence(),
                        buildSingleExtraRandomRevokedSequence(),
                    ),
                )
            }

            is SemanticCell.Transition.Update -> buildRandomEffectiveInputCellStimulationSequence(
                random = random,
                intermediateValueGenerator = noiseValueGenerator,
                inputCell = inputCell,
                newInputValue = semanticInputTransition.updatedValue,
            )
        }
    }

    private fun <ValueT> buildRandomEffectiveInputCellStimulationSequence(
        random: Random,
        intermediateValueGenerator: RandomValueGenerator<ValueT>,
        inputCell: TestInputCell<ValueT>,
        newInputValue: ValueT,
    ): TestSequentialStimulation {
        fun buildSingleExtraRandomRevokedStimulationSequence(): TestSequentialStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputCellStimulationSequence(
                    random = random,
                    noiseValueGenerator = intermediateValueGenerator,
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
                            correctedNewValue = newInputValue,
                        ),
                    ),
                )

                // Final change is the initial change
                else -> TestSequentialStimulation(
                    consecutiveStimulations = listOf(
                        inputCell.update(
                            newValue = newInputValue,
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
        noiseValueGenerator: RandomValueGenerator<ValueT>,
        inputCell: TestInputCell<ValueT>,
    ): TestSequentialStimulation {
        fun buildSingleExtraRandomCorrectionUpdateStimulation(): TestStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> inputCell.correctUpdate(
                    correctedNewValue = noiseValueGenerator.next(),
                )

                else -> null
            }
        }

        return TestSequentialStimulation(
            consecutiveStimulations = listOfNotNull(
                // We build at least one intermediate update
                inputCell.update(
                    newValue = noiseValueGenerator.next(),
                ),
                // But, with a small chance, we build up to two extra intermediate (correction) updates
                buildSingleExtraRandomCorrectionUpdateStimulation(),
                buildSingleExtraRandomCorrectionUpdateStimulation(),
                // Finally, the update sequence is revoked
                inputCell.revokeUpdate(),
            ),
        )
    }

    fun <SubjectValueT> buildExpectedSubjectCellValueTransition(
        semanticSemanticTransition: SemanticCell.Transition<SubjectValueT>,
    ): ExpectedCellValueTransition<SubjectValueT> = when (semanticSemanticTransition) {
        is SemanticCell.Transition.Pass -> Cell_expectations_testUtils.expectNoValueTransition(
            intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
            expectedUnaffectedValue = semanticSemanticTransition.unaffectedValue,
        )

        is SemanticCell.Transition.Update -> Cell_expectations_testUtils.expectValueTransition(
            intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
            expectedOldValue = semanticSemanticTransition.oldValue,
            expectedNewValue = semanticSemanticTransition.updatedValue,
        )
    }
}
