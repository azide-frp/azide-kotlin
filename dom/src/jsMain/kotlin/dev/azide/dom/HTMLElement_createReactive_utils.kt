package dev.azide.dom

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveList
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.Node

fun Document.creatingReactiveHtmlButtonElement(
    children: ReactiveList<Node>? = null,
): Effect<HTMLButtonElement> = creatingReactiveElement(
    createElement = Document::createButtonElement,
    children = children,
)

fun Document.creatingReactiveHtmlDivElement(
    children: ReactiveList<Node>? = null,
): Effect<HTMLDivElement> = creatingReactiveElement(
    createElement = Document::createDivElement,
    children = children,
)

fun Document.creatingReactiveHtmlSpanElement(
    children: ReactiveList<Node>? = null,
): Effect<HTMLSpanElement> = creatingReactiveElement(
    createElement = Document::createSpanElement,
    children = children,
)

fun Document.creatingReactiveHtmlInputElement(
    children: ReactiveList<Node>? = null,
): Effect<HTMLInputElement> = creatingReactiveElement(
    createElement = Document::createInputElement,
    children = children,
)
