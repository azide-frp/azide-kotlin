package dev.azide.dom.style

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Schedule
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.triggerEveryOf
import dev.azide.dom.pure.style.CssPropertyGroup
import org.w3c.dom.css.CSSStyleDeclaration

fun Cell<CssPropertyGroup>.bind(
    styleDeclaration: CSSStyleDeclaration,
): Schedule = triggerEveryOf { propertyGroup ->
    Action.adapt(
        externalTrigger = object : ExternalTrigger {
            override fun executeExternally() {
                propertyGroup.applyTo(
                    styleDeclaration = styleDeclaration,
                )
            }
        },
    )
}
