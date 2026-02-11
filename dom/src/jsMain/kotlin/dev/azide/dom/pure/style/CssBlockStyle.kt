package dev.azide.dom.pure.style

data class CssBlockStyle(
    override val outsideType: CssDisplayOutsideType? = null,
) : CssDualDisplayStyle() {
    override val insideType: CssDisplayInsideType = CssDisplayInsideType.Block

    override fun applySpecificDisplayProperties(
        applier: CssPropertyApplier,
    ) {
    }
}
