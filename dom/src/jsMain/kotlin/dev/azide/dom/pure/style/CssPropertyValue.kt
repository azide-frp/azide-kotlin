package dev.azide.dom.pure.style

abstract class CssPropertyValue {
    data class Dynamic(
        override val cssString: String,
    ) : CssPropertyValue()

    data class Number(
        val value: Double,
    ) : CssPropertyValue() {
        override val cssString: String
            get() = value.toString()
    }

    abstract val cssString: String
}
