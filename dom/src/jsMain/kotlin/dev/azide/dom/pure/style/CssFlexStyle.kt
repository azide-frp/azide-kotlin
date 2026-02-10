package dev.azide.dom.pure.style

import dev.azide.dom.pure.CssDimension

data class CssFlexStyle(
    override val outsideType: CssDisplayOutsideType? = null,
    val direction: CssFlexDirection? = null,
    val alignItems: CssFlexAlignItems? = null,
    val justifyContent: CssFlexJustifyContent? = null,
    val gap: CssDimension<*>? = null,
) : CssDualDisplayStyle() {
    override val insideType: CssDisplayInsideType = CssDisplayInsideType.Flex

    override fun applySpecificDisplayProperties(
        applier: CssPropertyApplier,
    ) {
        applier.applyProperty(
            kind = CssPropertyKind.FlexDirection,
            value = direction,
        )

        applier.applyProperty(
            kind = CssPropertyKind.AlignItems,
            value = alignItems,
        )

        applier.applyProperty(
            kind = CssPropertyKind.JustifyContent,
            value = justifyContent,
        )

        applier.applyProperty(
            kind = CssPropertyKind.Gap,
            value = gap,
        )
    }
}
