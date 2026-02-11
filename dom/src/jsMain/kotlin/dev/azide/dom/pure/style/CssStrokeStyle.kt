package dev.azide.dom.pure.style

import dev.azide.dom.pure.CssColor
import dev.azide.dom.pure.CssDimension

data class CssStrokeStyle(
    val color: CssColor? = null,
    val width: CssDimension<*>? = null,
) : CssPropertyGroup() {
    override fun applyProperties(applier: CssPropertyApplier) {
        applier.applyProperty(
            kind = CssPropertyKind.Stroke,
            value = color,
        )

        applier.applyProperty(
            kind = CssPropertyKind.StrokeWidth,
            value = width,
        )
    }
}
