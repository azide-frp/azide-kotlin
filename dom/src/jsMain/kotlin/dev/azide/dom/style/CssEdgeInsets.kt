package dev.azide.dom.style

import dev.azide.dom.pure.CssDimension
import dev.azide.dom.pure.style.CssPropertyApplier
import dev.azide.dom.pure.style.CssPropertyKind

data class CssEdgeInsets(
    val left: CssDimension<*>,
    val top: CssDimension<*>,
    val right: CssDimension<*>,
    val bottom: CssDimension<*>,
) {
    sealed class InsetKind() {
        data object Margin : InsetKind() {
            override val leftPropertyKind: CssPropertyKind = CssPropertyKind.MarginLeft

            override val topPropertyKind: CssPropertyKind = CssPropertyKind.MarginTop

            override val rightPropertyKind: CssPropertyKind = CssPropertyKind.MarginRight

            override val bottomPropertyKind: CssPropertyKind = CssPropertyKind.MarginBottom
        }

        data object Padding : InsetKind() {
            override val leftPropertyKind: CssPropertyKind = CssPropertyKind.PaddingLeft

            override val topPropertyKind: CssPropertyKind = CssPropertyKind.PaddingTop

            override val rightPropertyKind: CssPropertyKind = CssPropertyKind.PaddingRight

            override val bottomPropertyKind: CssPropertyKind = CssPropertyKind.PaddingBottom
        }

        abstract val leftPropertyKind: CssPropertyKind

        abstract val topPropertyKind: CssPropertyKind

        abstract val rightPropertyKind: CssPropertyKind

        abstract val bottomPropertyKind: CssPropertyKind

        fun clearProperties(
            applier: CssPropertyApplier,
        ) {
            applier.applyProperty(
                kind = leftPropertyKind,
                value = null,
            )

            applier.applyProperty(
                kind = topPropertyKind,
                value = null,
            )

            applier.applyProperty(
                kind = rightPropertyKind,
                value = null,
            )

            applier.applyProperty(
                kind = bottomPropertyKind,
                value = null,
            )
        }
    }

    companion object {
        fun all(
            value: CssDimension<*>,
        ): CssEdgeInsets = CssEdgeInsets(
            left = value,
            top = value,
            right = value,
            bottom = value,
        )
    }

    fun applyProperties(
        insetKind: InsetKind,
        applier: CssPropertyApplier,
    ) {
        applier.applyProperty(
            kind = insetKind.leftPropertyKind,
            value = left,
        )

        applier.applyProperty(
            kind = insetKind.topPropertyKind,
            value = top,
        )

        applier.applyProperty(
            kind = insetKind.rightPropertyKind,
            value = right,
        )

        applier.applyProperty(
            kind = insetKind.bottomPropertyKind,
            value = bottom,
        )
    }
}
