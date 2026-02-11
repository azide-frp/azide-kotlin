package dev.azide.dom.pure.style

sealed class CssDisplayInsideType(
    override val cssDisplayString: String,
) : CssDisplayType() {
    data object Flow : CssDisplayInsideType("flow")

    data object Flex : CssDisplayInsideType("flex")

    data object Block : CssDisplayInsideType("block")
}
