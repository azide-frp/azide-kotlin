package com.example.azide_example

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveList
import dev.azide.core.collections.ReactiveSet
import dev.azide.core.collections.actuateOf
import dev.azide.core.collections.helpers.SortableValue
import dev.azide.core.collections.helpers.withSortKey
import dev.azide.core.collections.sortedUniquely
import dev.azide.core.executeEachOf
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.joinOf
import dev.azide.core.map
import dev.azide.core.startExternally
import dev.azide.dom.creatingReactiveHtmlDivElement
import dev.azide.dom.creatingReactiveHtmlSpanElement
import dev.azide.dom.intervalTimeoutEffect
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import kotlin.time.Duration.Companion.seconds

fun creatingMagicNumberElement(
    magicNumber: Int,
): Effect<HTMLSpanElement> = document.creatingReactiveHtmlSpanElement(
    children = ReactiveList.of(
        document.createTextNode("$magicNumber"),
    ),
)

fun creatingRootElement(
    app: App,
): Effect<HTMLDivElement> = app.magicNumbers.actuateOf { magicNumber: Int ->
    creatingMagicNumberElement(magicNumber = magicNumber).map { magicNumberElement: HTMLSpanElement ->
        magicNumberElement withSortKey magicNumber
    }
}.joinOf { sortableMagicNumberElements: ReactiveBag<SortableValue<HTMLSpanElement, Int>> ->
    val sortedMagicNumberElements: ReactiveList<HTMLSpanElement> = sortableMagicNumberElements.sortedUniquely()

    document.creatingReactiveHtmlDivElement(
        children = sortedMagicNumberElements,
    )
}

class App {
    val magicNumbers = ReactiveSet.Const(
        setOf(10, 20, 30),
    )
}

fun main() {
    val timeoutStream = window.intervalTimeoutEffect(
        intervalDuration = 1.seconds,
    ).startExternally().result

    val app = App()

    timeoutStream.executeEachOf {
        Action.adapt(
            externalTrigger = object : ExternalTrigger {
                override fun executeExternally() {
                    println("Timeout!")
                }
            },
        )
    }.startExternally()

    val rootElement = creatingRootElement(
        app = app,
    ).startExternally().result

    document.body!!.append(rootElement)
}
