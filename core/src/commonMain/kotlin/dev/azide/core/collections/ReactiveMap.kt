package dev.azide.core.collections

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Effect

interface ReactiveMap<KeyT, out ValueT> {
    class Const<KeyT, out ValueT>(
        constEntries: Map<KeyT, ValueT>,
    ) : ReactiveMap<KeyT, ValueT>
}

fun <KeyT, ValueT> ReactiveMap<KeyT, ValueT>.containsKey(
    key: KeyT,
): Cell<Boolean> = TODO()

fun <KeyT, ValueT, TransformedValueT> ReactiveMap<KeyT, ValueT>.mapValues(
    transform: (KeyT, ValueT) -> TransformedValueT,
): ReactiveMap<KeyT, TransformedValueT> = TODO()

fun <KeyT, ValueT> ReactiveMap<KeyT, Cell<ValueT>>.fuseValues(): ReactiveMap<KeyT, ValueT> = TODO()

fun <KeyT, ValueT> ReactiveMap<KeyT, ValueT>.fuseValuesOf(
    selector: (KeyT, ValueT) -> Cell<ValueT>,
): ReactiveMap<KeyT, ValueT> = mapValues(selector).fuseValues()

fun <KeyT, ResultT> ReactiveMap<KeyT, Action<ResultT>>.executeEveryValue(): Effect<ReactiveMap<KeyT, ResultT>> = TODO()

fun <KeyT, ValueT, ResultT> ReactiveMap<KeyT, ValueT>.executeEveryValueOf(
    selector: (KeyT, ValueT) -> Action<ResultT>,
): Effect<ReactiveMap<KeyT, ResultT>> = mapValues(selector).executeEveryValue()

fun <KeyT, ResultT> ReactiveMap<KeyT, Effect<ResultT>>.actuateEveryValue(): Effect<ReactiveMap<KeyT, ResultT>> = TODO()

fun <KeyT, ValueT, ResultT> ReactiveMap<KeyT, ValueT>.actuateEveryValueOf(
    selector: (KeyT, ValueT) -> Effect<ResultT>,
): Effect<ReactiveMap<KeyT, ResultT>> = mapValues(selector).actuateEveryValue()
