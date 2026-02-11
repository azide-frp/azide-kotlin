package dev.azide.dom.pure.style

sealed class CssBoxSizing : CssPropertyValue() {
    data object BorderBox : CssBoxSizing() {
        override val cssString: String = "border-box"
    }

    data object ContentBox : CssBoxSizing() {
        override val cssString: String = "content-box"
    }
}
