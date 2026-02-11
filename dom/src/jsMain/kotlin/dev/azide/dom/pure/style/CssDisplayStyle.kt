package dev.azide.dom.pure.style

sealed class CssDisplayStyle : CssPropertyGroup() {
    final override fun applyProperties(
        applier: CssPropertyApplier,
    ) {
        applier.applyProperty(
            kind = CssPropertyKind.Display,
            value = displayType,
        )

        applySpecificDisplayProperties(
            applier = applier,
        )
    }

    abstract val displayType: CssDisplayType

    abstract fun applySpecificDisplayProperties(
        applier: CssPropertyApplier,
    )
}
