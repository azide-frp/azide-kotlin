package dev.azide.core.test_utils.stimulation_combinatorics

sealed interface TestSlotCount {
    val count: Int

    interface Count1Plus : TestSlotCount

    interface Count2Plus : Count1Plus

    data object Count2 : Count2Plus {
        override val count: Int = 2
    }

    interface Count3Plus : Count2Plus

    data object Count3 : Count3Plus {
        override val count: Int = 3
    }

    interface Count4Plus : Count3Plus

    data object Count4 : Count4Plus {
        override val count: Int = 4
    }

    interface Count5Plus : Count4Plus

    data object Count5 : Count5Plus {
        override val count: Int = 5
    }
}
