package dev.azide.dom.pure.style

sealed class CssFlexAlignItems(
    override val cssString: String,
) : CssPropertyValue() {

    companion object {
        fun parse(
            type: String,
        ): CssFlexAlignItems = when (type.lowercase()) {
            Start.cssString -> Start
            End.cssString -> End
            Center.cssString -> Center
            Baseline.cssString -> Baseline
            Stretch.cssString -> Stretch
            else -> throw IllegalArgumentException("Unsupported flex-align-items type: $type")
        }
    }

    data object Start : CssFlexAlignItems("flex-start")
    data object End : CssFlexAlignItems("flex-end")
    data object Center : CssFlexAlignItems("center")
    data object Baseline : CssFlexAlignItems("baseline")
    data object Stretch : CssFlexAlignItems("stretch")
}
