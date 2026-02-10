package dev.azide.dom.pure.style

sealed class CssFlexDirection(
    override val cssString: String,
) : CssPropertyValue() {
    companion object {
        fun parse(
            type: String,
        ): CssFlexDirection = when (type.lowercase()) {
            Row.cssString -> Row
            Column.cssString -> Column
            else -> throw IllegalArgumentException("Unsupported flex-direction type: $type")
        }
    }

    data object Row : CssFlexDirection("row")
    data object Column : CssFlexDirection("column")
}
