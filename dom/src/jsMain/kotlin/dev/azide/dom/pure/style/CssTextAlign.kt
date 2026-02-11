package dev.azide.dom.pure.style

sealed class CssTextAlign(
    override val cssString: String,
) : CssPropertyValue() {

    companion object {
        fun parse(
            type: String,
        ): CssTextAlign = when (type.lowercase()) {
            Start.cssString -> Start
            End.cssString -> End
            Left.cssString -> Left
            Right.cssString -> Right
            Center.cssString -> Center
            Justify.cssString -> Justify
            MatchParent.cssString -> MatchParent
            else -> throw IllegalArgumentException("Unsupported text-align type: $type")
        }
    }

    data object Start : CssTextAlign("start")
    data object End : CssTextAlign("end")
    data object Left : CssTextAlign("left")
    data object Right : CssTextAlign("right")
    data object Center : CssTextAlign("center")
    data object Justify : CssTextAlign("justify")
    data object MatchParent : CssTextAlign("match-parent")
}
