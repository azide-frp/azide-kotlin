package dev.azide.core.impl.utils.list

data class Uncons<T>(
    val firstElement: T,
    val trailingElement: List<T>,
)

fun <T> List<T>.uncons(): Uncons<T>? = firstOrNull()?.let { head ->
    Uncons(
        firstElement = head,
        trailingElement = drop(1),
    )
}
