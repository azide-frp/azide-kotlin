package dev.azide.core.internal.utils

object LoopUtils {
    inline fun <ResultT, LoopedValueT : Any> looped(
        block: (Lazy<LoopedValueT>) -> Pair<ResultT, LoopedValueT>,
    ): ResultT {
        val loopedLazy = LoopedLazy<LoopedValueT>()

        val (result, value) = block(loopedLazy)

        loopedLazy.loop(value)

        return result
    }
}
