package dev.azide.dom.pure.style

import dev.azide.dom.pure.CssDimension

data class CssFlexItemStyle(
    val basis: CssDimension<*>? = null,
    val grow: Double? = null,
    val shrink: Double? = null,
) : CssPropertyGroup() {
    override fun applyProperties(
        applier: CssPropertyApplier,
    ) {
        applier.applyProperty(
            kind = CssPropertyKind.FlexBasis,
            value = basis,
        )

        applier.applyProperty(
            kind = CssPropertyKind.FlexGrow,
            value = grow?.let { CssPropertyValue.Number(it) },
        )

        applier.applyProperty(
            kind = CssPropertyKind.FlexShrink,
            value = shrink?.let { CssPropertyValue.Number(it) },
        )
    }
}
