package dev.azide.dom.pure.style

sealed class CssFlexJustifyContent(
    override val cssString: String,
) : CssPropertyValue() {
    companion object {
        fun parse(
            type: String,
        ): CssFlexJustifyContent = when (type.lowercase()) {
            Start.cssString -> Start
            End.cssString -> End
            Center.cssString -> Center
            SpaceBetween.cssString -> SpaceBetween
            SpaceAround.cssString -> SpaceAround
            else -> throw IllegalArgumentException("Unsupported flex-justify-content type: $type")
        }
    }

    data object Start : CssFlexJustifyContent("flex-start")
    data object End : CssFlexJustifyContent("flex-end")
    data object Center : CssFlexJustifyContent("center")
    data object SpaceBetween : CssFlexJustifyContent("space-between")
    data object SpaceAround : CssFlexJustifyContent("space-around")
}
