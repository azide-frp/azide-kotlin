package com.example.azide_example

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveList
import dev.azide.core.executeEachOf
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.joinOf
import dev.azide.core.startExternally
import dev.azide.dom.creatingReactiveHtmlDivElement
import dev.azide.dom.creatingReactiveHtmlSpanElement
import dev.azide.dom.intervalTimeoutEffect
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import kotlin.time.Duration.Companion.seconds

fun creatingMessageElement(): Effect<HTMLSpanElement> = document.creatingReactiveHtmlSpanElement(
    children = ReactiveList.of(
        document.createTextNode("Hello!"),
    ),
)

fun creatingRootElement(): Effect<HTMLDivElement> = creatingMessageElement().joinOf { messageElement: HTMLSpanElement ->
    document.creatingReactiveHtmlDivElement(
        children = ReactiveList.of(
            messageElement,
        ),
    )
}

fun main() {
    val timeoutStream = window.intervalTimeoutEffect(
        intervalDuration = 1.seconds,
    ).startExternally().result

    timeoutStream.executeEachOf {
        Action.adapt(
            externalTrigger = object : ExternalTrigger {
                override fun executeExternally() {
                    println("Timeout!")
                }
            },
        )
    }.startExternally()

    val rootElement = creatingRootElement().startExternally().result

    document.body!!.append(rootElement)
}
