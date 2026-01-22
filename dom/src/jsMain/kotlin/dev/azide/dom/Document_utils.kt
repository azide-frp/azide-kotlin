package dev.azide.dom

import dev.azide.core.Effect
import dev.azide.core.MomentContext
import dev.azide.core.collections.ReactiveList
import dev.azide.dom.collections.childNodesList
import dev.azide.dom.components.Component
import dev.azide.dom.style.ReactiveStyle
import dev.toolkt.reactive.effect.Actions
import dev.toolkt.reactive.effect.Effect
import dev.toolkt.reactive.effect.MomentContext
import dev.toolkt.reactive.effect.joinOf
import dev.toolkt.reactive.effect.startBound
import dev.toolkt.reactive.reactive_list.ReactiveList
import dev.toolkt.reactive.reactive_list.actuateOf
import dev.toolkt.reactive.reactive_list.bind
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.css.ElementCSSInlineStyle

/**
 * Creates a reactive element of type [ElementT] in the [Document].
 *
 * Binds to [ElementCSSInlineStyle.style] and [Node] child list.
 */
context(momentContext: MomentContext) fun <ElementT> Document.creatingReactiveElement(
    createElement: Document.() -> ElementT,
    children: ReactiveList<Node>? = null,
): Effect<ElementT> where ElementT : Element, ElementT : ElementCSSInlineStyle = createElement().also { element ->
    children
}
