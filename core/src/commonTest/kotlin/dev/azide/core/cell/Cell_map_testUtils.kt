package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.sampleExternally
import dev.azide.core.test_utils.assertIsInactive
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_offlineActivation_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.generic.TestSubjectHealthChecker
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
object Cell_map_testUtils {
    private class MapHealthChecker(
        private val inputCell: TestInputCell<Int>,
    ) : generic_reaction_testUtils.CellHealthChecker<String> {
        override fun verifyInputsInactive() {
            assertIsInactive(
                testInputEntity = inputCell,
                inputEntityLabel = "input cell",
            )
        }

        override fun prepareHealthCheck(
            subject: Cell<String>,
        ): TestSubjectHealthChecker.HealthCheckDescription<Cell<String>, CellVertex.Update<String>> {
            val expectedPreHealthCheckValue = inputCell.sampleExternally().toString()

            return TestSubjectHealthChecker.HealthCheckDescription(
                inputStimulation = inputCell.update(
                    newValue = -1234,
                ),
                expectedSubjectTransition = Cell_expectations_testUtils.expectValueTransition(
                    expectedOldValue = expectedPreHealthCheckValue,
                    expectedNewValue = "-1234",
                ),
            )
        }
    }

    fun executeOfflineActivationTransaction(
        inputCell: TestInputCell<Int>,
        subjectCell: Cell<String>,
    ) {
        Cell_offlineActivation_testUtils.executeOfflineActivationTransaction(
            subjectCell = subjectCell,
            subjectHealthChecker = MapHealthChecker(inputCell = inputCell),
        )
    }
}
