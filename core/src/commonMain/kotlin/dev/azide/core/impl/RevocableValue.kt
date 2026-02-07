package dev.azide.core.impl

interface RevocableValue<ValueT> : Revocable {
    val value: ValueT
}
