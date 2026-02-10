package dev.azide.dom.pure.style

data class CssDualDisplayType(
    val outsideType: CssDisplayOutsideType?,
    val insideType: CssDisplayInsideType,
) : CssDisplayType() {
    override val cssDisplayString: String
        get() = listOfNotNull(
            outsideType?.cssString,
            insideType.cssString,
        ).joinToString(separator = " ")
}
