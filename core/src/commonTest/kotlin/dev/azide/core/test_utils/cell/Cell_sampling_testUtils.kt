package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestStimulation

@Suppress("ClassName")
data object Cell_sampling_testUtils {
    fun <ValueT> executeSamplingTransaction(
        subjectCell: Cell<ValueT>,
        inputStimulation: TestStimulation? = null,
        expectedSubjectValue: ExpectedCellValue<ValueT>,
    ) {
        Transactions.execute { propagationContext ->
            inputStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            expectedSubjectValue.verifyStableState(
                propagationContext = propagationContext,
                subject = subjectCell,
            )
        }
    }
}
