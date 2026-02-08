package dev.azide.core.test_utils

interface TestStimulationSlot

val TestStimulationSlot.ordinal: Int
    get() = (this as Enum<*>).ordinal
