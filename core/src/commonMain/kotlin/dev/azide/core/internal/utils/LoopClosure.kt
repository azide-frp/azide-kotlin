package dev.azide.core.internal.utils

data class LoopClosure<ResultT, LoopedValueT : Any>(
    val result: ResultT,
    val loopedValue: LoopedValueT,
)
