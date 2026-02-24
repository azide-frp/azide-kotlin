package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.sampleExternally
import dev.azide.core.test_utils.assertIsInactive
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_offlineActivation_testUtils
import dev.azide.core.test_utils.cell.ExpectedCellValueTransition
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import dev.azide.core.test_utils.generic.TestSubjectHealthChecker
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
object Cell_map2_testUtils {
    private class Map2HealthChecker(
        private val inputCell1: TestInputCell<Int>,
        private val inputCell2: TestInputCell<Char>,
    ) : generic_reaction_testUtils.CellHealthChecker<String> {
        override fun verifyInputsInactive() {
            assertIsInactive(
                testInputEntity = inputCell1,
                inputEntityLabel = "input cell 1",
            )
            assertIsInactive(
                testInputEntity = inputCell2,
                inputEntityLabel = "input cell 2",
            )
        }

        override fun prepareHealthCheck(
            subject: Cell<String>,
        ): TestSubjectHealthChecker.HealthCheckDescription<Cell<String>, CellVertex.Update<String>> {
            val expectedPreHealthCheckValue = "${inputCell1.sampleExternally()}:${inputCell2.sampleExternally()}"

            return TestSubjectHealthChecker.HealthCheckDescription(
                inputStimulation = inputCell1.update(
                    newValue = -1234,
                ),
                expectedSubjectTransition = Cell_expectations_testUtils.expectValueTransition(
                    expectedOldValue = expectedPreHealthCheckValue,
                    expectedNewValue = "-1234:${inputCell2.sampleExternally()}",
                ),
            )
        }
    }

    fun testOfflineActivation(
        inputCell1: TestInputCell<Int>,
        inputCell2: TestInputCell<Char>,
        subjectCell: Cell<String>,
    ) {
        Cell_offlineActivation_testUtils.testOfflineActivation(
            subjectCell = subjectCell,
            subjectHealthChecker = Map2HealthChecker(inputCell1 = inputCell1, inputCell2 = inputCell2),
        )
    }

    fun testReaction(
        inputCell1: TestInputCell<Int>,
        inputCell2: TestInputCell<Char>,
        inputStimulationPlan: generic_reaction_testUtils.InputStimulationPlan,
        subjectCell: Cell<String>,
        expectedSubjectValueTransition: ExpectedCellValueTransition<String>,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        generic_reaction_testUtils.testReaction(
            trait = CellObservationTrait(),
            subject = subjectCell,
            inputStimulationPlan = inputStimulationPlan,
            expectedSubjectTransition = expectedSubjectValueTransition,
            subjectHealthChecker = Map2HealthChecker(inputCell1 = inputCell1, inputCell2 = inputCell2),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }
}

