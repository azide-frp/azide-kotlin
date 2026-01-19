package dev.azide.core.impl.utils

fun <T, R> Lazy<T>.map(
    transform: (T) -> R,
): Lazy<R> = lazy {
    transform(this.value)
}
