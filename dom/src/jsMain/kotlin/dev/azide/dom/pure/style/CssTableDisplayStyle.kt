package dev.azide.dom.pure.style

import dev.azide.dom.pure.CssDimension

data class CssTableDisplayStyle(
    val borderCollapse: BorderCollapse? = null,
    val borderSpacing: CssDimension<*>? = null,
) : CssDisplayStyle() {
    data object Row : CssDisplayStyle() {
        override val displayType = CssDisplayType.TableRow

        override fun applySpecificDisplayProperties(
            applier: CssPropertyApplier,
        ) {
        }
    }

    data object Cell : CssDisplayStyle() {
        override val displayType = CssDisplayType.TableCell

        override fun applySpecificDisplayProperties(
            applier: CssPropertyApplier,
        ) {
        }
    }

    sealed class BorderCollapse : CssPropertyValue() {
        data object Collapse : BorderCollapse() {
            override val cssString: String = "collapse"
        }

        data object Separate : BorderCollapse() {
            override val cssString: String = "separate"
        }
    }

    override val displayType = CssDisplayType.Table

    override fun applySpecificDisplayProperties(
        applier: CssPropertyApplier,
    ) {
        applier.applyProperty(
            kind = CssPropertyKind.BorderCollapse,
            value = borderCollapse,
        )

        applier.applyProperty(
            kind = CssPropertyKind.BorderSpacing,
            value = borderSpacing,
        )
    }
}
