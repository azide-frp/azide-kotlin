package dev.azide.dom

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveList
import dev.azide.dom.style.ReactiveCssStyle
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.Node
import org.w3c.dom.Text

fun Document.creatingReactiveHtmlButtonElement(
    children: ReactiveList<Node>? = null,
    style: ReactiveCssStyle = ReactiveCssStyle.Default,
): Effect<HTMLButtonElement> = creatingReactiveElement(
    createElement = Document::createButtonElement,
    children = children,
    style = style,
)

fun Document.creatingReactiveHtmlDivElement(
    children: ReactiveList<Node>? = null,
    style: ReactiveCssStyle = ReactiveCssStyle.Default,
): Effect<HTMLDivElement> = creatingReactiveElement(
    createElement = Document::createDivElement,
    children = children,
    style = style,
)

fun Document.creatingReactiveHtmlSpanElement(
    children: ReactiveList<Node>? = null,
    style: ReactiveCssStyle = ReactiveCssStyle.Default,
): Effect<HTMLSpanElement> = creatingReactiveElement(
    createElement = Document::createSpanElement,
    children = children,
    style = style,
)

fun Document.creatingReactiveHtmlInputElement(
    children: ReactiveList<Node>? = null,
    style: ReactiveCssStyle = ReactiveCssStyle.Default,
): Effect<HTMLInputElement> = creatingReactiveElement(
    createElement = Document::createInputElement,
    children = children,
    style = style,
)
