package dev.azide.dom.pure.style

sealed class CssDisplayType() : CssPropertyValue() {
    data object Table : CssDisplayType() {
        override val cssDisplayString: String = "table"
    }

    data object TableRow : CssDisplayType() {
        override val cssDisplayString: String = "table-row"
    }

    data object TableCell : CssDisplayType() {
        override val cssDisplayString: String = "table-cell"
    }

    final override val cssString: String
        get() = cssDisplayString

    abstract val cssDisplayString: String
}
