package dev.azide.dom

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveList
import dev.azide.core.collections.syncing
import dev.azide.core.external.ExternalAllocator
import dev.azide.core.mapTo
import dev.azide.core.startingOf
import dev.azide.dom.collections.childNodesList
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.css.ElementCSSInlineStyle

/**
 * Creates a reactive element of type [ElementT] in the [Document].
 *
 * Binds to [ElementCSSInlineStyle.style] and [Node] child list.
 */
fun <ElementT> Document.creatingReactiveElement(
    createElement: Document.() -> ElementT,
    children: ReactiveList<Node>? = null,
): Effect<ElementT> where ElementT : Element, ElementT : ElementCSSInlineStyle {
    val effectiveChildren = children ?: ReactiveList.empty()

    return Action.alloc(
        externalAllocator = object : ExternalAllocator<ElementT> {
            override fun allocateExternally(): ElementT = createElement()
        },
    ).startingOf { element: ElementT ->
        effectiveChildren.syncing(
            externalMutableList = element.childNodesList,
        ).mapTo(element)
    }
}
