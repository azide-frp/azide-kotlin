package dev.azide.core.impl.utils

data class LoopClosure<ResultT, LoopedValueT : Any>(
    val result: ResultT,
    val loopedValue: LoopedValueT,
)
