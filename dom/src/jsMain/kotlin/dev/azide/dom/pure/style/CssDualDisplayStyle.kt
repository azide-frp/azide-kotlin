package dev.azide.dom.pure.style

sealed class CssDualDisplayStyle : CssDisplayStyle() {
    abstract val outsideType: CssDisplayOutsideType?

    abstract val insideType: CssDisplayInsideType

    override val displayType: CssDualDisplayType
        get() = CssDualDisplayType(
            outsideType = outsideType,
            insideType = insideType,
        )
}
