package dev.azide.dom.pure.style

interface CssPropertyApplier {
    fun applyProperty(
        kind: CssPropertyKind,
        value: CssPropertyValue?,
    )
}

abstract class CssPropertyGroup {
    abstract fun applyProperties(
        applier: CssPropertyApplier,
    )
}
