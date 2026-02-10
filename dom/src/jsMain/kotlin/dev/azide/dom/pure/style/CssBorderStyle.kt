package dev.azide.dom.pure.style

import dev.azide.dom.pure.CssColor
import dev.azide.dom.pure.CssDimension

data class CssBorderStyle(
    val width: CssDimension<*>? = null,
    val color: CssColor? = null,
    val style: Style? = null,
) : CssPropertyGroup() {
    sealed class Style : CssPropertyValue() {
        data object None : Style() {
            override val cssString: String = "none"
        }

        data object Hidden : Style() {
            override val cssString: String = "hidden"
        }

        data object Dotted : Style() {
            override val cssString: String = "dotted"
        }

        data object Dashed : Style() {
            override val cssString: String = "dashed"
        }

        data object Solid : Style() {
            override val cssString: String = "solid"
        }

        data object Double : Style() {
            override val cssString: String = "double"
        }

        data object Groove : Style() {
            override val cssString: String = "groove"
        }

        data object Ridge : Style() {
            override val cssString: String = "ridge"
        }

        data object Inset : Style() {
            override val cssString: String = "inset"
        }

        data object Outset : Style() {
            override val cssString: String = "outset"
        }
    }

    override fun applyProperties(applier: CssPropertyApplier) {
        applier.applyProperty(
            kind = CssPropertyKind.BorderWidth,
            value = width,
        )

        applier.applyProperty(
            kind = CssPropertyKind.BorderColor,
            value = color,
        )

        applier.applyProperty(
            kind = CssPropertyKind.BorderStyle,
            value = style,
        )
    }
}
