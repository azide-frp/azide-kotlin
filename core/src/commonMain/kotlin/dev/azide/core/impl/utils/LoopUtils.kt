package dev.azide.core.impl.utils

object LoopUtils {
    inline fun <ResultT, LoopedValueT : Any> looped(
        block: (Lazy<LoopedValueT>) -> LoopClosure<ResultT, LoopedValueT>,
    ): ResultT {
        val loopedLazy = LoopedLazy<LoopedValueT>()

        val (result, value) = block(loopedLazy)

        loopedLazy.loop(value)

        return result
    }

    inline fun <ResultT : Any> selfLooped(
        block: (Lazy<ResultT>) -> ResultT,
    ): ResultT = looped { resultLazy: Lazy<ResultT> ->
        val result = block(resultLazy)

        LoopClosure(
            result = result,
            loopedValue = result,
        )
    }
}
