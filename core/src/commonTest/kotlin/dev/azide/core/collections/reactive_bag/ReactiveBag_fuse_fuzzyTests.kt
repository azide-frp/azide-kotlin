package dev.azide.core.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.fuse
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.TestStimulationSequence
import dev.azide.core.test_utils.cell.Cell_fuzzyTestUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_reaction_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.DynamicInterleavingUtils
import kotlin.jvm.JvmInline
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveBag_fuse_fuzzyTests {
    @JvmInline
    value class MyBagTag(
        val id: Int,
    ) {
        companion object {
            val allIds = (0 until maxInputBagSize).map { MyBagTag(it) }.toSet()

            fun next(
                random: Random,
            ): MyBagTag = MyBagTag(
                id = random.nextInt(0 until maxInputBagSize),
            )
        }

        init {
            require(id < maxInputBagSize) {
                "MyBagTag id must be less than $maxInputBagSize, but was $id."
            }
        }
    }

    @JvmInline
    value class InputCellId(
        val id: Int,
    ) {
        companion object {
            val allIds = (0 until totalInputCellCount).map { InputCellId(it) }.toSet()

            fun next(
                random: Random,
            ): InputCellId = InputCellId(
                id = random.nextInt(0 until totalInputCellCount),
            )
        }

        init {
            require(id < totalInputCellCount) {
                "InputCellId id must be less than $totalInputCellCount, but was $id."
            }
        }

        fun nextStringValue(
            random: Random,
        ): String = stringValue(
            payloadValue = random.nextInt(1..maxCellPayloadValue),
        )

        fun stringValue(
            payloadValue: Int,
        ) = "#$id:$payloadValue"
    }

    companion object {
        private const val iterationCount = 1_000
        private const val maxInputBagSize = 100
        private const val totalInputCellCount = 400

        private const val initialCellValue = 0
        private const val maxCellPayloadValue = 1000
    }

    @Test
    @Ignore
    fun test_fuzzy_fromEmpty() {
        test_fuzzy(
            initialInputBagSize = 0,
        )
    }

    @Test
    @Ignore
    fun test_fuzzy_fromNonEmpty() {
        test_fuzzy(
            initialInputBagSize = maxInputBagSize / 2,
        )
    }

    private fun test_fuzzy(
        initialInputBagSize: Int,
    ) {
        require(initialInputBagSize < maxInputBagSize) {
            "Initial input bag size must be less than $maxInputBagSize, but was $initialInputBagSize."
        }

        val random = Random(0)

        // Create all input cells
        val inputCellById: Map<InputCellId, TestInputCell<String>> = InputCellId.allIds.associateWith { inputCellId ->
            TestInputCell(
                initialValue = inputCellId.stringValue(payloadValue = initialCellValue),
            )
        }

        // Initialize with random values
        var currentInputCellValueById: Map<InputCellId, String> = inputCellById.mapValues { (inputCellId, _) ->
            inputCellId.stringValue(payloadValue = initialCellValue)
        }

        var currentExposedCellIdByTag: Map<MyBagTag, InputCellId> = (0 until initialInputBagSize).associate { tagId ->
            MyBagTag(id = tagId) to InputCellId.next(random = random)
        }

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = TaggedBag.ofTaggedContent(
                currentExposedCellIdByTag.mapValues { (_, inputCellId) ->
                    inputCellById[inputCellId]!!
                },
            ),
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        repeat(iterationCount) {
            val oldInputCellValueById = currentInputCellValueById
            val oldExposedCellIdByTag = currentExposedCellIdByTag

            // Build new state for this iteration
            val newInputCellValueById = buildNewInputCellValueById(
                random = random,
                oldInputCellValueById = oldInputCellValueById,
            )

            val newExposedCellIdByTag = buildRandomPossiblyChangedExposedCellIdByTag(
                random = random,
                oldExposedCellIdByTag = oldExposedCellIdByTag,
            )

            // Build all stimulation sequences
            val cellStimulationSequences = buildInputCellStimulationSequences(
                random = random,
                inputCellById = inputCellById,
                oldInputCellValueById = oldInputCellValueById,
                newInputCellValueById = newInputCellValueById,
            )

            val bagStimulationSequence = buildAppropriateInputBagStimulationSequence(
                random = random,
                inputCellById = inputCellById,
                inputReactiveBag = inputReactiveBag,
                oldExposedCellIdByTag = oldExposedCellIdByTag,
                newExposedCellIdByTag = newExposedCellIdByTag,
            )

            val allStimulationSequences = cellStimulationSequences + bagStimulationSequence

            val combinedInputStimulation = TestStimulation.combine(
                stimulations = DynamicInterleavingUtils.generateRandom(
                    random = random,
                    lists = allStimulationSequences.mapNotNull { it?.consecutiveStimulations },
                ),
            )

            // Calculate expected old and new tagged elements
            val expectedOldTaggedElements = TaggedBag.ofTaggedContent(
                oldExposedCellIdByTag.mapValues { (_, inputCellId) ->
                    oldInputCellValueById[inputCellId]!!
                },
            )

            val expectedNewTaggedElements = TaggedBag.ofTaggedContent(
                newExposedCellIdByTag.mapValues { (_, inputCellId) ->
                    newInputCellValueById[inputCellId]!!
                },
            )

            // Execute the reaction transaction
            ReactiveBag_reaction_testUtils.testReaction(
                subjectReactiveBag = subjectReactiveBag,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        combinedInputStimulation,
                    ),
                ),
                expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                    intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                    expectedOldTaggedElements = expectedOldTaggedElements,
                    expectedNewTaggedElements = expectedNewTaggedElements,
                ),
            )

            // Update state for next iteration
            currentInputCellValueById = newInputCellValueById
            currentExposedCellIdByTag = newExposedCellIdByTag
        }
    }

    private fun buildNewInputCellValueById(
        random: Random,
        oldInputCellValueById: Map<InputCellId, String>,
    ): Map<InputCellId, String> {
        val r0 = random.nextDouble()

        if (r0 < 0.1) {
            // Small chance to keep all cell values the same
            return oldInputCellValueById
        }

        return oldInputCellValueById.mapValues { (inputCellId, oldInputCellValue) ->
            buildNewInputCellValue(
                random = random,
                inputCellId = inputCellId,
                oldInputCellValue = oldInputCellValue,
            )
        }
    }

    private fun buildNewInputCellValue(
        random: Random,
        inputCellId: InputCellId,
        oldInputCellValue: String,
    ): String {
        val r = random.nextDouble()

        return when {
            r < 0.5 -> {
                // Some chance to keep the cell's value
                oldInputCellValue
            }

            else -> inputCellId.nextStringValue(random = random)
        }
    }

    /**
     * Build a new exposed cell id by tag mapping which has some chance of being the same as the old one, but most
     * likely will have some differences.
     */
    private fun buildRandomPossiblyChangedExposedCellIdByTag(
        random: Random,
        oldExposedCellIdByTag: Map<MyBagTag, InputCellId>,
    ): Map<MyBagTag, InputCellId> {
        val r0 = random.nextDouble()

        if (r0 < 0.1) {
            // Small chance to keep all exposed cells the same
            return oldExposedCellIdByTag
        }

        return buildRandomLikelyChangedExposedCellIdByTag(
            random = random,
            oldExposedCellIdByTag = oldExposedCellIdByTag,
        )
    }

    /**
     * Build a new exposed cell id by tag mapping which has a very high chance of being different from the old one,
     * but without guarantees. It's always possible (and very likely) to generate an effectively changed mapping.
     */
    private fun buildRandomLikelyChangedExposedCellIdByTag(
        random: Random,
        oldExposedCellIdByTag: Map<MyBagTag, InputCellId>,
    ): Map<MyBagTag, InputCellId> {
        val adjustedExposedCellIdByTag = oldExposedCellIdByTag.entries.mapNotNull { (oldBagTag, oldExposedCellId) ->
            val r = random.nextDouble()

            when {
                // Some chance to remove the exposed cell for this tag
                r < 0.2 -> null
                // Some chance to change the exposed cell for this tag to a random cell
                r < 0.4 -> oldBagTag to InputCellId.next(random = random)
                // Otherwise, keep the same exposed cell for this tag
                else -> oldBagTag to oldExposedCellId
            }
        }.toMap()

        val unusedTags = (MyBagTag.allIds - oldExposedCellIdByTag.keys).toList()

        val maxAddedEntryCount = (unusedTags.size / 4).coerceAtLeast(2).coerceAtMost(unusedTags.size)
        val addedEntryCount = random.nextInt(maxAddedEntryCount)

        val addedExposedCellIdByTag = unusedTags.take(addedEntryCount).associateWith { _ ->
            InputCellId.next(random = random)
        }

        // If the _adjusted_ map is (randomly) the same as the old map and the added map is (randomly) empty, the
        // returned map will be the same as the old map
        return adjustedExposedCellIdByTag + addedExposedCellIdByTag
    }

    private fun buildInputCellStimulationSequences(
        random: Random,
        inputCellById: Map<InputCellId, TestInputCell<String>>,
        oldInputCellValueById: Map<InputCellId, String>,
        newInputCellValueById: Map<InputCellId, String>,
    ): Set<TestStimulationSequence> = inputCellById.mapNotNull { (inputCellId, inputCell) ->
        val oldValue = oldInputCellValueById[inputCellId]!!
        val newValue = newInputCellValueById[inputCellId]!!

        Cell_fuzzyTestUtils.buildAppropriateInputCellStimulationSequence(
            random = random,
            intermediateValueGenerator = object : RandomValueGenerator<String> {
                override fun next(): String = inputCellId.nextStringValue(random = random) + "~"
            },
            inputCell = inputCell,
            oldValue = oldValue,
            newValue = newValue,
        )
    }.toSet()

    private fun buildAppropriateInputBagStimulationSequence(
        random: Random,
        inputCellById: Map<InputCellId, TestInputCell<String>>,
        inputReactiveBag: TestInputReactiveBag<TestInputCell<String>>,
        oldExposedCellIdByTag: Map<MyBagTag, InputCellId>,
        newExposedCellIdByTag: Map<MyBagTag, InputCellId>,
    ): TestStimulationSequence? {
        fun buildSingleExtraRandomRevokedSequence(): TestStimulationSequence? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputBagStimulationSequence(
                    random = random,
                    inputCellById = inputCellById,
                    inputReactiveBag = inputReactiveBag,
                    oldExposedCellIdByTag = oldExposedCellIdByTag,
                )

                else -> null
            }
        }

        val effectiveChangeDescription = buildInputBagChangeDescriptionOrNull(
            inputCellById = inputCellById,
            oldExposedCellIdByTag = oldExposedCellIdByTag,
            changedExposedCellIdByTag = newExposedCellIdByTag,
        )

        return when {
            // There's no actual change in the structure
            effectiveChangeDescription == null -> {
                TestStimulationSequence.concatAll(
                    sequences = listOfNotNull(
                        // We build up to two ineffective revoked change sequences
                        buildSingleExtraRandomRevokedSequence(),
                        buildSingleExtraRandomRevokedSequence(),
                    ),
                )
            }

            // There's an actual change in the structure, we build an effective change sequence
            else -> buildRandomEffectiveInputBagStimulationSequence(
                random = random,
                inputCellById = inputCellById,
                inputReactiveBag = inputReactiveBag,
                oldExposedCellIdByTag = oldExposedCellIdByTag,
                finalChangeDescription = effectiveChangeDescription,
            )
        }
    }

    private fun buildRandomEffectiveInputBagStimulationSequence(
        random: Random,
        inputCellById: Map<InputCellId, TestInputCell<String>>,
        inputReactiveBag: TestInputReactiveBag<TestInputCell<String>>,
        oldExposedCellIdByTag: Map<MyBagTag, InputCellId>,
        finalChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>,
    ): TestStimulationSequence {
        fun buildSingleExtraRandomRevokedStimulationSequence(): TestStimulationSequence? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> buildRandomRevokedInputBagStimulationSequence(
                    random = random,
                    inputCellById = inputCellById,
                    inputReactiveBag = inputReactiveBag,
                    oldExposedCellIdByTag = oldExposedCellIdByTag,
                )

                else -> null
            }
        }

        fun buildSingleExtraRandomCorrectionStimulation(): TestStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> inputReactiveBag.correctChange(
                    correctedChangeDescription = buildRandomEffectiveInputBagChangeDescription(
                        random = random,
                        inputCellById = inputCellById,
                        oldExposedCellIdByTag = oldExposedCellIdByTag,
                    ),
                )

                else -> null
            }
        }

        fun buildFinalStimulationSequence(): TestStimulationSequence {
            val r = random.nextDouble()

            return when {
                // Final change is correction
                r < 0.3 -> TestStimulationSequence(
                    consecutiveStimulations = listOfNotNull(
                        inputReactiveBag.change(
                            changeDescription = buildRandomEffectiveInputBagChangeDescription(
                                random = random,
                                inputCellById = inputCellById,
                                oldExposedCellIdByTag = oldExposedCellIdByTag,
                            ),
                        ),
                        // Build a potential extra correction change
                        buildSingleExtraRandomCorrectionStimulation(),
                        inputReactiveBag.correctChange(
                            correctedChangeDescription = finalChangeDescription,
                        ),
                    ),
                )

                // Final change the initial change
                else -> TestStimulationSequence(
                    consecutiveStimulations = listOf(
                        inputReactiveBag.change(
                            changeDescription = finalChangeDescription,
                        ),
                    ),
                )
            }
        }

        return TestStimulationSequence.concatAll(
            sequences = listOfNotNull(
                // Build up to two extra random revoked change sequences which should be totally ineffective
                buildSingleExtraRandomRevokedStimulationSequence(),
                buildSingleExtraRandomRevokedStimulationSequence(),
                buildFinalStimulationSequence(),
            ),
        )!!
    }

    private fun buildRandomRevokedInputBagStimulationSequence(
        random: Random,
        inputCellById: Map<InputCellId, TestInputCell<String>>,
        inputReactiveBag: TestInputReactiveBag<TestInputCell<String>>,
        oldExposedCellIdByTag: Map<MyBagTag, InputCellId>,
    ): TestStimulationSequence {
        fun buildSingleExtraRandomCorrectionChangeStimulation(): TestStimulation? {
            val r = random.nextDouble()

            return when {
                r < 0.1 -> inputReactiveBag.correctChange(
                    correctedChangeDescription = buildRandomEffectiveInputBagChangeDescription(
                        random = random,
                        inputCellById = inputCellById,
                        oldExposedCellIdByTag = oldExposedCellIdByTag,
                    ),
                )

                else -> null
            }
        }

        return TestStimulationSequence(
            consecutiveStimulations = listOfNotNull(
                // We build at least one intermediate change
                inputReactiveBag.change(
                    changeDescription = buildRandomEffectiveInputBagChangeDescription(
                        random = random,
                        inputCellById = inputCellById,
                        oldExposedCellIdByTag = oldExposedCellIdByTag,
                    ),
                ),
                // But, with a small chance, we build up to two extra intermediate (correction) changes
                buildSingleExtraRandomCorrectionChangeStimulation(),
                buildSingleExtraRandomCorrectionChangeStimulation(),
                // Revoke the change
                inputReactiveBag.revokeChange(),
            ),
        )
    }

    private fun buildRandomEffectiveInputBagChangeDescription(
        random: Random,
        inputCellById: Map<InputCellId, TestInputCell<String>>,
        oldExposedCellIdByTag: Map<MyBagTag, InputCellId>,
    ): TestInputReactiveBag.ChangeDescription<TestInputCell<String>> {
        repeat(10) { // Eventually we have to draw an effective change...
            val randomExposedCellIdByTag = buildRandomLikelyChangedExposedCellIdByTag(
                random = random,
                oldExposedCellIdByTag = oldExposedCellIdByTag,
            )

            val randomChangeDescription = buildInputBagChangeDescriptionOrNull(
                inputCellById = inputCellById,
                oldExposedCellIdByTag = oldExposedCellIdByTag,
                changedExposedCellIdByTag = randomExposedCellIdByTag,
            )

            if (randomChangeDescription != null) {
                return randomChangeDescription
            }
        }

        throw UnsupportedOperationException("Failed to generate a random effective change description after multiple attempts.")
    }

    private fun buildInputBagChangeDescriptionOrNull(
        inputCellById: Map<InputCellId, TestInputCell<String>>,
        oldExposedCellIdByTag: Map<MyBagTag, InputCellId>,
        changedExposedCellIdByTag: Map<MyBagTag, InputCellId>,
    ): TestInputReactiveBag.ChangeDescription<TestInputCell<String>>? {
        val addedExposedCellByTag =
            (changedExposedCellIdByTag - oldExposedCellIdByTag.keys).mapValues { (_, inputCellId) ->
                inputCellById[inputCellId]!!
            }

        val replacedExposedCellByTag = changedExposedCellIdByTag.mergeValuesPresentInBoth(
            other = oldExposedCellIdByTag,
        ) { newInputCellId, oldInputCellId ->
            when {
                newInputCellId == oldInputCellId -> null
                else -> inputCellById[newInputCellId]!!
            }
        }.filterValuesNotNull()

        val removedTags = oldExposedCellIdByTag.keys - changedExposedCellIdByTag.keys

        return TestInputReactiveBag.ChangeDescription.of(
            addedElementByTag = addedExposedCellByTag.typeErased(),
            replacedElementByTag = replacedExposedCellByTag.typeErased(),
            removedTags = removedTags,
        )
    }

    /**
     * Helper function to erase the specific key/tag type of map.
     */
    private fun <ElementT> Map<MyBagTag, ElementT>.typeErased(): Map<ReactiveBag.Tag, ElementT> =
        this.entries.associate { (tag, element) ->
            val tag: ReactiveBag.Tag = tag
            tag to element
        }
}

private fun <K, V : Any> Map<K, V?>.filterValuesNotNull(): Map<K, V> = this.entries.mapNotNull { (key, value) ->
    value?.let { key to it }
}.toMap()

private fun <K, V1, V2, R> Map<out K, V1>.mergeValuesPresentInBoth(
    other: Map<K, V2>,
    merge: (V1, V2) -> R,
): Map<K, R> = this.entries.mapNotNull { (key, value1) ->
    val value2 = other[key] ?: return@mapNotNull null
    key to merge(value1, value2)
}.toMap()
