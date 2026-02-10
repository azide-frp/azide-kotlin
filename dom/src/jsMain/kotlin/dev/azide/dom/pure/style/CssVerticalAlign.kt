package dev.azide.dom.pure.style

sealed class CssVerticalAlign(
    override val cssString: String,
) : CssPropertyValue() {
    companion object {
        fun parse(
            type: String,
        ): CssVerticalAlign = when (type.lowercase()) {
            Baseline.cssString -> Baseline
            Sub.cssString -> Sub
            Super.cssString -> Super
            TextTop.cssString -> TextTop
            TextBottom.cssString -> TextBottom
            Middle.cssString -> Middle
            Top.cssString -> Top
            Bottom.cssString -> Bottom
            else -> throw IllegalArgumentException("Unsupported vertical-align type: $type")
        }
    }

    data object Baseline : CssVerticalAlign("baseline")
    data object Sub : CssVerticalAlign("sub")
    data object Super : CssVerticalAlign("super")
    data object TextTop : CssVerticalAlign("text-top")
    data object TextBottom : CssVerticalAlign("text-bottom")
    data object Middle : CssVerticalAlign("middle")
    data object Top : CssVerticalAlign("top")
    data object Bottom : CssVerticalAlign("bottom")
}
