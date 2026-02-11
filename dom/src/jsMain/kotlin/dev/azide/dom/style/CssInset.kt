package dev.azide.dom.style

import dev.azide.dom.pure.CssDimension
import dev.azide.dom.pure.px
import dev.azide.dom.pure.style.CssPropertyApplier
import dev.azide.dom.pure.style.CssPropertyGroup
import dev.azide.dom.pure.style.CssPropertyKind

data class CssInset(
    val top: CssDimension<*>,
    val right: CssDimension<*>,
    val bottom: CssDimension<*>,
    val left: CssDimension<*>,
) : CssPropertyGroup() {
    companion object {
        val Zero = CssInset(
            top = 0.px,
            right = 0.px,
            bottom = 0.px,
            left = 0.px,
        )
    }

    override fun applyProperties(applier: CssPropertyApplier) {
        applier.applyProperty(
            kind = CssPropertyKind.Top,
            value = top,
        )

        applier.applyProperty(
            kind = CssPropertyKind.Right,
            value = right,
        )

        applier.applyProperty(
            kind = CssPropertyKind.Bottom,
            value = bottom,
        )

        applier.applyProperty(
            kind = CssPropertyKind.Left,
            value = left,
        )
    }
}
