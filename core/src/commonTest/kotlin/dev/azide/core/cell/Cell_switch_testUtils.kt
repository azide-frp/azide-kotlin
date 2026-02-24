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
object Cell_switch_testUtils {
    private class SwitchHealthChecker(
        private val outerCell: TestInputCell<*>,
        private vararg val innerCells: TestInputCell<Int>,
    ) : generic_reaction_testUtils.CellHealthChecker<Int> {
        override fun verifyInputsInactive() {
            // verify outer cell inactive
            assertIsInactive(
                testInputEntity = outerCell,
                inputEntityLabel = "outer cell",
            )

            // verify all inner cells inactive
            innerCells.forEachIndexed { index, cell ->
                assertIsInactive(
                    testInputEntity = cell,
                    inputEntityLabel = "inner cell #${index + 1}",
                )
            }
        }

        override fun prepareHealthCheck(
            subject: Cell<Int>,
        ): TestSubjectHealthChecker.HealthCheckDescription<Cell<Int>, CellVertex.Update<Int>> {
            // Use the first provided inner cell as the one to stimulate for the health check
            val primaryInner = innerCells.first()
            val expectedPreHealthCheckValue = primaryInner.sampleExternally()

            return TestSubjectHealthChecker.HealthCheckDescription(
                inputStimulation = primaryInner.update(
                    newValue = -1234,
                ),
                expectedSubjectTransition = Cell_expectations_testUtils.expectValueTransition(
                    expectedOldValue = expectedPreHealthCheckValue,
                    expectedNewValue = -1234,
                ),
            )
        }
    }

    fun testOfflineActivation(
        outerCell: TestInputCell<*>,
        newInnerCell: TestInputCell<Int>,
        subjectCell: Cell<Int>,
    ) {
        Cell_offlineActivation_testUtils.testOfflineActivation(
            subjectCell = subjectCell,
            subjectHealthChecker = SwitchHealthChecker(outerCell = outerCell, innerCells = *arrayOf(newInnerCell)),
        )
    }

    fun testReaction(
        outerCell: TestInputCell<*>,
        newInnerCell: TestInputCell<Int>,
        inputStimulationPlan: generic_reaction_testUtils.InputStimulationPlan,
        subjectCell: Cell<Int>,
        expectedSubjectValueTransition: ExpectedCellValueTransition<Int>,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        generic_reaction_testUtils.testReaction(
            trait = CellObservationTrait(),
            subject = subjectCell,
            inputStimulationPlan = inputStimulationPlan,
            expectedSubjectTransition = expectedSubjectValueTransition,
            subjectHealthChecker = SwitchHealthChecker(outerCell = outerCell, innerCells = *arrayOf(newInnerCell)),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }
}

