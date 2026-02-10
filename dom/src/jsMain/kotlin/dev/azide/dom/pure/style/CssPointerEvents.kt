package dev.azide.dom.pure.style

sealed class CssPointerEvents : CssPropertyValue() {
    data object Auto : CssPointerEvents() {
        override val cssString: String = "auto"
    }

    data object None : CssPointerEvents() {
        override val cssString: String = "none"
    }

    // SVG-only
    data object All : CssPointerEvents() {
        override val cssString: String = "all"
    }
}
