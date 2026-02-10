package dev.azide.dom.style

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Schedule
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.triggerEveryOf
import dev.azide.dom.pure.style.CssPropertyKind
import dev.azide.dom.pure.style.CssPropertyValue
import org.w3c.dom.css.CSSStyleDeclaration

fun Cell<CssPropertyValue>.bind(
    styleDeclaration: CSSStyleDeclaration,
    kind: CssPropertyKind,
): Schedule = triggerEveryOf { propertyValue ->
    Action.adapt(
        externalTrigger = object : ExternalTrigger {
            override fun executeExternally() {
                styleDeclaration.setOrRemoveProperty(
                    kind = kind,
                    value = propertyValue,
                )
            }
        },
    )
}

fun CssPropertyValue.applyTo(
    styleDeclaration: CSSStyleDeclaration,
    kind: CssPropertyKind,
) {
    styleDeclaration.setOrRemoveProperty(
        kind = kind,
        value = this,
    )
}
