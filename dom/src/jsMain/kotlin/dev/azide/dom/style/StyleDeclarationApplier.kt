package dev.azide.dom.style

import dev.azide.dom.pure.style.CssPropertyApplier
import dev.azide.dom.pure.style.CssPropertyGroup
import dev.azide.dom.pure.style.CssPropertyKind
import dev.azide.dom.pure.style.CssPropertyValue
import org.w3c.dom.css.CSSStyleDeclaration

fun CssPropertyGroup.applyTo(
    styleDeclaration: CSSStyleDeclaration,
) {
    applyProperties(
        applier = StyleDeclarationApplier(
            styleDeclaration = styleDeclaration,
        ),
    )
}

internal class StyleDeclarationApplier(
    private val styleDeclaration: CSSStyleDeclaration,
) : CssPropertyApplier {
    override fun applyProperty(
        kind: CssPropertyKind,
        value: CssPropertyValue?,
    ) {
        styleDeclaration.setOrRemoveProperty(
            kind = kind,
            value = value,
        )
    }
}

internal fun CSSStyleDeclaration.setOrRemoveProperty(
    /**
     * The name of the CSS property to set or remove.
     */
    kind: CssPropertyKind,
    /**
     * The value to set for the property. If null (or empty), the property will be removed.
     */
    value: CssPropertyValue?,
) {
    when {
        value == null || value.cssString.isEmpty() -> {
            this.removeProperty(kind.cssName)
        }

        else -> {
            this.setProperty(kind.cssName, value.cssString)
        }
    }
}
