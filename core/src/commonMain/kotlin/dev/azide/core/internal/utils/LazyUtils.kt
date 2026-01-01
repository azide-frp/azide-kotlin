package dev.azide.core.internal.utils

object LazyUtils {
    inline fun <ResultT, LoopedValueT : Any> looped(
        block: (Lazy<LoopedValueT>) -> Pair<ResultT, LoopedValueT>,
    ): ResultT {
        val loopedLazy = LoopedLazy<LoopedValueT>()

        val (result, value) = block(loopedLazy)

        loopedLazy.loop(value)

        return result
    }
}

fun <ValueT, TransformedValueT> Lazy<ValueT>.map(
    transform: (ValueT) -> TransformedValueT,
): Lazy<TransformedValueT> = object : Lazy<TransformedValueT> {
    override val value: TransformedValueT
        get() = transform(this@map.value)

    override fun isInitialized(): Boolean = this@map.isInitialized()
}
