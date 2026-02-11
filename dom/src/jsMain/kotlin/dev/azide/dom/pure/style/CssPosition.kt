package dev.azide.dom.pure.style

sealed class CssPosition(
    override val cssString: String,
) : CssPropertyValue() {
    data object Static : CssPosition("static")

    data object Relative : CssPosition("relative")

    data object Absolute : CssPosition("absolute")

    data object Fixed : CssPosition("fixed")

    data object Sticky : CssPosition("sticky")
}
