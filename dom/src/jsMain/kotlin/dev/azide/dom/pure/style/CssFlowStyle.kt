package dev.azide.dom.pure.style

data class CssFlowStyle(
    override val outsideType: CssDisplayOutsideType? = null,
) : CssDualDisplayStyle() {
    override val insideType: CssDisplayInsideType = CssDisplayInsideType.Flow

    override fun applySpecificDisplayProperties(
        applier: CssPropertyApplier,
    ) {
    }
}
