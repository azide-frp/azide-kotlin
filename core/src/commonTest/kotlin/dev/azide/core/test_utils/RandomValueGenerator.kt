package dev.azide.core.test_utils

interface RandomValueGenerator<T> {
    fun next(): T
}
