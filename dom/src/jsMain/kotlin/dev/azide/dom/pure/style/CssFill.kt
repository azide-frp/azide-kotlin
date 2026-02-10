package dev.azide.dom.pure.style

import dev.azide.dom.pure.CssColor

sealed class CssFill() : CssPropertyValue() {
    data object None : CssFill() {
        override val cssString: String = "none"
    }

    data class Colored(
        val color: CssColor,
    ) : CssFill() {
        override val cssString: String
            get() = color.cssString
    }
}
